package com.example.firebaseui_firestoreexample.firestore_data;

import com.example.firebaseui_firestoreexample.CloudUser;
import com.google.firebase.firestore.DocumentReference;

public class CloudUserData {
    private CloudUser cloudUser;
    private DocumentReference documentReference;

    public CloudUserData(CloudUser cloudUser, DocumentReference documentReference) {
        this.cloudUser = cloudUser;
        this.documentReference = documentReference;
    }

    public CloudUser getCloudUser() {
        return cloudUser;
    }

    public void setCloudUser(CloudUser cloudUser) {
        this.cloudUser = cloudUser;
    }

    public DocumentReference getDocumentReference() {
        return documentReference;
    }

    public void setDocumentReference(DocumentReference documentReference) {
        this.documentReference = documentReference;
    }
}
