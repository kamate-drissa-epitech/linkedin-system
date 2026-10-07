package com.linkedin.searchservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

/**
 * Document for user search
 * Index when user register or update
 * Enable full text search across name, headline, location...
 */
@Document(indexName = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDocument {
    @Id
    private String id;


    @Field(type = FieldType.Keyword)
    private String email;

    @Field(type = FieldType.Text)
    private String firstName;

    @Field(type = FieldType.Text)
    private String lastName;

    @Field(type = FieldType.Text)
    private String headline;


    @Field(type = FieldType.Keyword)
    private List<String> skills;

    @Field(type = FieldType.Keyword)
    private String location;

    private String profilePhotoUrl;






}
