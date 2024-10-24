package de.abat.shortener.url.pool;

import de.abat.shortener.url.exceptions.KeyNotFoundInPoolException;

public interface KeyPool {

    /**
     * Get Unique key from Pool and remove it
     *
     * @return unique x-length key each time
     */
    String pop();

    /**
     * Get the custom key and remove it from pool
     *
     * @param custom the key should remove from getAndRemovepool
     * @return custom key
     */
    String popCustom(String custom) throws KeyNotFoundInPoolException;

    default String pop(String maybeKey) {
        if (maybeKey == null) {
            return pop();
        } else {
            return popCustom(maybeKey);
        }
    }
}
