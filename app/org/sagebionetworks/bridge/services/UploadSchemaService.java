package org.sagebionetworks.bridge.services;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.sagebionetworks.bridge.dao.UploadSchemaDao;
import org.sagebionetworks.bridge.exceptions.BadRequestException;
import org.sagebionetworks.bridge.exceptions.InvalidEntityException;
import org.sagebionetworks.bridge.models.studies.Study;
import org.sagebionetworks.bridge.models.upload.UploadSchema;
import org.sagebionetworks.bridge.validators.UploadSchemaValidator;
import org.sagebionetworks.bridge.validators.Validate;

/**
 * Service handler for upload schema APIs. This is called by researchers to create, read, and update upload schemas.
 */
@Component
public class UploadSchemaService {
    private UploadSchemaDao uploadSchemaDao;

    /** DAO for upload schemas. This is configured by Spring. */
    @Autowired
    public void setUploadSchemaDao(UploadSchemaDao uploadSchemaDao) {
        this.uploadSchemaDao = uploadSchemaDao;
    }

    /**
     * <p>
     * Service handler for creating and updating upload schemas. This method creates an upload schema, using the study
     * ID and schema ID of the specified schema, or updates an existing one if it already exists.
     * </p>
     * <p>
     * This method validates the schema. However, it does not validate the study, as that is not user input.
     * </p>
     *
     * @param study
     *         the study this schema should be created or updated in, provided by the controller
     * @param uploadSchema
     *         schema to create or update, must be non-null, must contain a valid schema ID
     * @return the created or updated schema, will be non-null
     */
    public UploadSchema createOrUpdateUploadSchema(Study study, UploadSchema uploadSchema) {
        // validate schema
        if (uploadSchema == null) {
            throw new InvalidEntityException(String.format(Validate.CANNOT_BE_NULL, "upload schema"));
        }
        Validate.entityThrowingException(UploadSchemaValidator.INSTANCE, uploadSchema);

        // call through to DAO
        return uploadSchemaDao.createOrUpdateUploadSchema(study.getIdentifier(), uploadSchema);
    }

    /**
     * <p>
     * Service handler for fetching upload schemas. This method fetches an upload schema for the specified study and
     * schema ID. If there is more than one revision of the schema, this fetches the latest revision. If the schema
     * doesn't exist, this handler throws an InvalidEntityException.
     * </p>
     * <p>
     * This method validates the schema ID. However, it does not validate the study, as that is not user input.
     * </p>
     *
     * @param study
     *         study to fetch the schema from, provided by the controller
     * @param schemaId
     *         ID of the schema to fetch, must be non-null and non-empty
     * @return the fetched schema, will be non-null
     */
    public UploadSchema getUploadSchema(Study study, String schemaId) {
        if (Strings.isNullOrEmpty(schemaId)) {
            throw new BadRequestException(String.format("Invalid schema ID %s", schemaId));
        }
        return uploadSchemaDao.getUploadSchema(study.getIdentifier(), schemaId);
    }
}