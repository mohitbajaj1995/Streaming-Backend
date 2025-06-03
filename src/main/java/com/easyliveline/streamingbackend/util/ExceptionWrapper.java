package com.easyliveline.streamingbackend.util;

import com.easyliveline.streamingbackend.exceptions.CustomQueryException;
import com.easyliveline.streamingbackend.exceptions.InsufficientPointsException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.PersistenceException;
import org.hibernate.HibernateException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionSystemException;

public class ExceptionWrapper {

    public static <T> T handle(CheckedSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (IllegalArgumentException ex) {
            throw new CustomQueryException("INVALID_ARGUMENT","" ,"Invalid query argument: " + extractShortMessage(ex), ex);
        } catch (org.hibernate.QueryException ex) {
            throw new CustomQueryException("HQL_QUERY_ERROR","" ,"HQL query failed: " + extractShortMessage(ex), ex);
        } catch (jakarta.validation.ConstraintViolationException | org.hibernate.exception.ConstraintViolationException ex) {
            throw new CustomQueryException("CONSTRAINT_VIOLATION","" ,"Validation or DB constraint failed: " + extractShortMessage(ex), ex);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("DataIntegrityViolation: " + ex.getMessage(), ex);
        } catch (EntityExistsException ex) {
            throw new IllegalArgumentException("EntityExistsException: " + ex.getMessage(), ex);
        } catch (TransactionSystemException ex) {
            throw new IllegalArgumentException("Invalid data provided. Please check your input values.", ex);
        } catch (HibernateException ex) {
            throw new CustomQueryException("HIBERNATE_ERROR","" ,"Hibernate error occurred: " + extractShortMessage(ex), ex);
        } catch (PersistenceException ex) {
            throw new CustomQueryException("QUERY_PARSE_ERROR","" ,"Persistence error occurred: " + extractShortMessage(ex), ex);
        } catch (InsufficientPointsException ex) {
            throw new InsufficientPointsException("InsufficientPointsException occurred: " + extractShortMessage(ex));
        } catch (RuntimeException ex) {
            throw new CustomQueryException("RUNTIME_ERROR","" ,"Unexpected runtime error occurred -> " + extractShortMessage(ex), ex);
        } catch (Exception ex) {
            throw new CustomQueryException("GENERIC_ERROR","" ,"Unexpected error occurred while querying or saving data", ex);
        }
    }


    private static String extractShortMessage(Throwable ex) {
        return ex.getMessage() != null ? ex.getMessage().split("\n")[0] : "No message available";
    }

    public static void handleVoid(CheckedRunnable runnable) {
        handle(() -> {
            runnable.run();
            return null;
        });
    }
}