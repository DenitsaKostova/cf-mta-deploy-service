package com.sap.cloud.lm.sl.cf.web.api.model;

import java.math.BigInteger;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

public class FileMetadata {

    private String id = null;
    private String name = null;
    private BigInteger size = null;
    private String digest = null;
    private String digestAlgorithm = null;
    private String space = null;

    /**
     **/
    public FileMetadata id(String id) {
        this.id = id;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     **/
    public FileMetadata name(String name) {
        this.name = name;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     **/
    public FileMetadata size(BigInteger size) {
        this.size = size;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("size")
    public BigInteger getSize() {
        return size;
    }

    public void setSize(BigInteger size) {
        this.size = size;
    }

    /**
     **/
    public FileMetadata digest(String digest) {
        this.digest = digest;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("digest")
    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    /**
     **/
    public FileMetadata digestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("digestAlgorithm")
    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    /**
     **/
    public FileMetadata space(String space) {
        this.space = space;
        return this;
    }

    @ApiModelProperty(value = "")
    @JsonProperty("space")
    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FileMetadata fileMetadata = (FileMetadata) o;
        return Objects.equals(id, fileMetadata.id) && Objects.equals(name, fileMetadata.name) && Objects.equals(size, fileMetadata.size)
            && Objects.equals(digest, fileMetadata.digest) && Objects.equals(digestAlgorithm, fileMetadata.digestAlgorithm)
            && Objects.equals(space, fileMetadata.space);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, size, digest, digestAlgorithm, space);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class FileMetadata {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    size: ").append(toIndentedString(size)).append("\n");
        sb.append("    digest: ").append(toIndentedString(digest)).append("\n");
        sb.append("    digestAlgorithm: ").append(toIndentedString(digestAlgorithm)).append("\n");
        sb.append("    space: ").append(toIndentedString(space)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
