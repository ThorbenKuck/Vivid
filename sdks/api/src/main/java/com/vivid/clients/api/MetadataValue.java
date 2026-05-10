package com.vivid.clients.api;

import com.fasterxml.jackson.annotation.*;
import com.vivid.clients.api.metadata.*;
import jakarta.annotation.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@type",
        visible = true,
        defaultImpl = UnknownMetadataValue.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BooleanMetadataValue.class, name = "Boolean"),
        @JsonSubTypes.Type(value = LongMetadataValue.class, name = "Long"),
        @JsonSubTypes.Type(value = DoubleMetadataValue.class, name = "Double"),
        @JsonSubTypes.Type(value = StringMetadataValue.class, name = "String"),
        @JsonSubTypes.Type(value = JsonMetadataValue.class, name = "Json"),
        @JsonSubTypes.Type(value = StringListMetadataValue.class, name = "StringList")
})
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonPropertyOrder({"@type", "content"})
public interface MetadataValue<T> {

    T getContent();

    @Nullable
    default Boolean isTrue() {
        if (this instanceof BooleanMetadataValue b) {
            return b.getContent();
        } else if (this instanceof StringMetadataValue s) {
            return Boolean.parseBoolean(s.getContent());
        } else {
            return null;
        }
    }

    @Nullable
    default Boolean isFalse() {
        if (this instanceof BooleanMetadataValue b) {
            return !b.getContent();
        } else if (this instanceof StringMetadataValue s) {
            return !Boolean.parseBoolean(s.getContent());
        } else {
            return null;
        }
    }

    @Nullable
    default Boolean contains(String value) {
        if (this instanceof StringListMetadataValue b) {
            return b.getContent().contains(value);
        } else if (this instanceof StringMetadataValue s) {
            return s.getContent().contains(value);
        } else {
            return null;
        }
    }

    @Nullable
    default Boolean isBiggerThan(Double value) {
        if (this instanceof DoubleMetadataValue d) {
            return d.getContent() > value;
        } else if (this instanceof LongMetadataValue l) {
            return l.getContent() > value;
        } else {
            return null;
        }
    }

    @Nullable
    default Boolean isBiggerThan(Long value) {
        if (this instanceof DoubleMetadataValue d) {
            return d.getContent() > value;
        } else if (this instanceof LongMetadataValue l) {
            return l.getContent() > value;
        } else {
            return null;
        }
    }

    @Nullable
    default Boolean isSmallerThan(Double value) {
        if (this instanceof DoubleMetadataValue d) {
            return d.getContent() < value;
        } else if (this instanceof LongMetadataValue l) {
            return l.getContent() < value;
        } else {
            return null;
        }
    }

    @Nullable
    default Boolean isSmallerThan(Long value) {
        if (this instanceof DoubleMetadataValue d) {
            return d.getContent() < value;
        } else if (this instanceof LongMetadataValue l) {
            return l.getContent() < value;
        } else {
            return null;
        }
    }
}
