package org.sagebionetworks.bridge.services;

import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.FORBIDDEN;

import java.util.List;

import org.sagebionetworks.bridge.dao.StudyConsentDao;
import org.sagebionetworks.bridge.exceptions.BridgeServiceException;
import org.sagebionetworks.bridge.models.StudyConsent;
import org.sagebionetworks.bridge.models.StudyConsentForm;
import org.sagebionetworks.bridge.models.User;

public class StudyConsentServiceImpl implements StudyConsentService {

    private StudyConsentDao studyConsentDao;

    public void setStudyConsentDao(StudyConsentDao studyConsentDao) {
        this.studyConsentDao = studyConsentDao;
    }

    @Override
    public StudyConsent addConsent(User caller, String studyKey, StudyConsentForm form) {
        assertAdminUser(caller);
        return studyConsentDao.addConsent(studyKey, form.getPath(), form.getMinAge());
    }

    @Override
    public StudyConsent getActiveConsent(User caller, String studyKey) {
        assertAdminUser(caller);

        StudyConsent consent = studyConsentDao.getConsent(studyKey);
        if (consent == null) {
            throw new BridgeServiceException("There is no active consent document.", BAD_REQUEST);
        }
        return consent;
    }

    @Override
    public List<StudyConsent> getAllConsents(User caller, String studyKey) {
        assertAdminUser(caller);
        return studyConsentDao.getConsents(studyKey);
    }

    @Override
    public StudyConsent getConsent(User caller, String studyKey, long timestamp) {
        assertAdminUser(caller);
        return studyConsentDao.getConsent(studyKey, timestamp);
    }

    @Override
    public StudyConsent activateConsent(User caller, String studyKey, long timestamp) {
        assertAdminUser(caller);
        
        StudyConsent studyConsent = studyConsentDao.getConsent(studyKey, timestamp);
        return studyConsentDao.setActive(studyConsent, true);
    }

    @Override
    public void deleteConsent(User caller, String studyKey, long timestamp) {
        assertAdminUser(caller);

        if (studyConsentDao.getConsent(studyKey, timestamp).getActive()) {
            throw new BridgeServiceException("Cannot delete active consent document.", BAD_REQUEST);
        }

        studyConsentDao.deleteConsent(studyKey, timestamp);
    }
    
    private void assertAdminUser(User user) {
        if (!user.isInRole("admin")) {
            throw new BridgeServiceException("Must be admin to add consent document.", FORBIDDEN);
        }
    }

}
