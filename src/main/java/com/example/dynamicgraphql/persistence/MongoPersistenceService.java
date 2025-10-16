package com.example.dynamicgraphql.persistence;

import com.example.dynamicgraphql.config.EngineProperties;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.UpdateResult;
import java.time.Instant;
import java.util.Map;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MongoPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(MongoPersistenceService.class);

    private final EngineProperties properties;
    private final MongoClient mongoClient;

    public MongoPersistenceService(EngineProperties properties, MongoClient mongoClient) {
        this.properties = properties;
        this.mongoClient = mongoClient;
    }

    public void upsert(Map<String, Object> document) {
        MongoCollection<Document> collection = collection();
        Document dbDocument = new Document(document);
        Object id = dbDocument.get("_id");
        if (id == null) {
            throw new IllegalArgumentException("Document must contain an _id field");
        }

        long previousVersion = extractVersion(dbDocument);
        long nextVersion = previousVersion + 1;
        Instant now = Instant.now();
        dbDocument.put("updatedAt", now);
        dbDocument.putIfAbsent("createdAt", now);
        dbDocument.put("version", nextVersion);

        Document filter = new Document("_id", id);
        boolean insert = previousVersion == 0;
        if (!insert) {
            filter.put("version", previousVersion);
        }

        ReplaceOptions options = new ReplaceOptions().upsert(insert);
        UpdateResult result = collection.replaceOne(filter, dbDocument, options);
        if (!insert && result.getMatchedCount() == 0) {
            throw new OptimisticLockingFailureException(id.toString(), previousVersion);
        }
        log.debug("Document {} persisted at version {}", id, nextVersion);
    }

    private MongoCollection<Document> collection() {
        MongoDatabase database = mongoClient.getDatabase(properties.getPersistence().getDatabase());
        return database.getCollection(properties.getPersistence().getCollection());
    }

    private long extractVersion(Document document) {
        Object version = document.get("version");
        if (version == null) {
            return 0L;
        }
        if (version instanceof Number number) {
            return number.longValue();
        }
        if (version instanceof String string) {
            try {
                return Long.parseLong(string);
            } catch (NumberFormatException ignored) {
                log.warn("Ignoring non-numeric version {}", string);
            }
        }
        return 0L;
    }
}
