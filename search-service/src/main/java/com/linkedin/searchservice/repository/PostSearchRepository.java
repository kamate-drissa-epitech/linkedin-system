package com.linkedin.searchservice.repository;

import com.linkedin.searchservice.model.PostDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.sql.Connection;
import java.util.List;

public interface PostSearchRepository extends ElasticsearchRepository<PostDocument, String> {
    /**
     * Search post
     * @param query
     * @return
     */
    @Query("""
    {
        "match": {
                "content": {
                    "query": "?0",
                    "fuzziness": "AUTO"
                }
        }
    }
    """)
    List<PostDocument> searchPosts(String query );
}
