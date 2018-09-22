package com.peoplethink.governmentjob.providers.woocommerce.interceptor;

/**
 * This interface for nonce and timestamp
 */
public interface TimestampService
{
    /**
     * Returns the unix epoch timestamp in seconds
     *
     * @return timestamp
     */
    String getTimestampInSeconds();

    /**
     * Returns a nonce (unique value for each request)
     *
     * @return nonce
     */
    String getNonce();
}

