/**
 * Created by irufus on 2/19/15.
 */
package com.rwe.platform.rest;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;

import java.util.List;
import java.util.Map;

public interface IRestService {

     String getBaseUrl();

     <R> HttpEntity<String> securityHeaders(String uri, String resourcePath, String method, String jsonBody);

     <T> T get(String endpoint, Map<String, ?> uriVariables, ParameterizedTypeReference<T> type);

     <T> T get(String endpoint, ParameterizedTypeReference<T> type);

     <T> T pagedGet(String endpoint, Map<String, ?> uriVariables, ParameterizedTypeReference<T> responseType, String beforeOrAfter, Integer pageNumber, Integer limit);

     <T> List<T> getAsList(String resourcePath, ParameterizedTypeReference<T[]> responseType);

     <T> List<T> getAsList(String endpoint, Map<String, ?> uriVariables, ParameterizedTypeReference<T[]> type);

     <T> List<T> pagedGetAsList(String endpoint, Map<String, ?> uriVariables, ParameterizedTypeReference<T[]> responseType, String beforeOrAfter, Integer pageNumber, Integer limit);

     <T, R> T post(String endpoint, Map<String, ?> uriVariables, ParameterizedTypeReference<T> type, R jsonObject);

     <T, R> T post(String endpoint, ParameterizedTypeReference<T> type, R jsonObject);

     <T> T delete(String endpoint, Map<String, ?> uriVariables, ParameterizedTypeReference<T> type);

     <T> T delete(String endpoint, ParameterizedTypeReference<T> type);
}
