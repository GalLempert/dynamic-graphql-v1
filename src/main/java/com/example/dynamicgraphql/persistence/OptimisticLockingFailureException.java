package com.example.dynamicgraphql.persistence;

public class OptimisticLockingFailureException extends RuntimeException {

    public OptimisticLockingFailureException(String documentId, long expectedVersion) {
        super("Optimistic locking failure for document " + documentId + " expected version " + expectedVersion);
    }
}
