package net.maidsafe.sample.services;

import android.content.Context;

import net.maidsafe.api.Client;
import net.maidsafe.api.Session;
import net.maidsafe.api.listener.OnDisconnected;
import net.maidsafe.api.model.NativeHandle;
import net.maidsafe.safe_app.MDataEntry;
import net.maidsafe.safe_app.MDataInfo;
import net.maidsafe.safe_app.MDataValue;

import java.util.List;
import java.util.concurrent.ExecutionException;

public final class SafeApi {

    private static final SafeApi INSTANCE = new SafeApi();
    private static boolean loaded;
    private Session session;
    private String appId;
    private static final String LIST_KEY = "myToDoLists";
    private static final String APP_CONTAINER_NAME = "apps/";
    private static final String NO_SUCH_DATA_ERROR_CODE = "-106";
    private static final String DATA_NOT_FOUND_EXCEPTION = "-103";
    private static final int TYPE_TAG = 16290;

    private SafeApi() {
    }

    public static SafeApi getInstance(final Context context) {
        if (!loaded) {
            Client.load(context);
            loaded = true;
        }
        return INSTANCE;
    }

    public void connect(final String response, final String applicationId,
                        final OnDisconnected onDisconnected) throws Exception {
        // Establish connection with the SAFE Network
    }

    public MDataInfo getSectionsFromAppContainer() throws Exception {
        MDataInfo info;
        final MDataInfo appContainerInfo = session.getContainerMDataInfo(APP_CONTAINER_NAME + appId).get();
        final byte[] encryptedKey = session.mData.encryptEntryKey(appContainerInfo, LIST_KEY.getBytes()).get();
        try {
            final MDataValue mDataValue = session.mData.getValue(appContainerInfo, encryptedKey).get();
            if (mDataValue.getContentLen() <= 0) {
                info = initAppData(appContainerInfo);
            } else {
                final byte[] serializedMdInfo = session.mData.decrypt(appContainerInfo, mDataValue.getContent()).get();
                info = session.mData.deserialise(serializedMdInfo).get();
            }
        } catch (ExecutionException e) {
            if (e.getMessage().contains(NO_SUCH_DATA_ERROR_CODE)) {
                info = initAppData(appContainerInfo);
            } else {
                throw e;
            }
        }
        return info;
    }

    public MDataInfo initAppData(final MDataInfo appContainerInfo) throws Exception {
        final MDataInfo mdInfo = newMutableData(TYPE_TAG);
        saveMdInfo(appContainerInfo, mdInfo);
        return  mdInfo;
    }

    public List<MDataEntry> getEntries(final MDataInfo mDataInfo) throws Exception {
        // Fetch a list of entries from the network
        return null;
    }

    public void insertPermissions(final MDataInfo mDataInfo) throws Exception {
        // Insert permissions and PUT the MData to the network
    }

    public void addEntry(final byte[] key, final byte[] value, final MDataInfo mDataInfo) throws Exception {
        // Add an entry and mutate the data
    }

    public void deleteEntry(final byte[] key, final long version, final MDataInfo mDataInfo) throws Exception {
       // Delete an entry in the Mutable data
    }

    public void updateEntry(final byte[] key, final byte[] newValue, final long version,
                            final MDataInfo mDataInfo) throws Exception {
       // Update existing Mutable data
    }

    public long getEntriesLength(final MDataInfo mDataInfo) throws Exception {
        long length;
        try {
            final NativeHandle entriesHandle = session.mData.getEntriesHandle(mDataInfo).get();
            length = session.mDataEntries.length(entriesHandle).get();
        } catch (ExecutionException e) {
            if (e.getMessage().contains(DATA_NOT_FOUND_EXCEPTION)) {
                length = 0;
            } else {
                throw e;
            }
        }
        return length;
    }

    public MDataInfo newMutableData(final long tagType) throws Exception {
        // Create new mutable data
        return null;
    }

    private void saveMdInfo(final MDataInfo appContainerInfo, final MDataInfo mDataInfo) throws Exception {
        final byte[] serializedMdInfo = session.mData.serialise(mDataInfo).get();
        final byte[] encryptedContainerKey = session.mData.encryptEntryKey(appContainerInfo, LIST_KEY.getBytes()).get();
        final byte[] encryptedMdInfo = session.mData.encryptEntryValue(appContainerInfo, serializedMdInfo).get();
        final NativeHandle containerEntry = session.mDataEntryAction.newEntryAction().get();
        session.mDataEntryAction.insert(containerEntry, encryptedContainerKey, encryptedMdInfo).get();
        session.mData.mutateEntries(appContainerInfo, containerEntry);
    }

    public byte[] decryptEntryValue(final MDataInfo mDataInfo, final MDataEntry mDataEntry) throws Exception {
        // Decrypt data
        return null;
    }

    public byte[] serializeMdInfo(final MDataInfo mDataInfo) throws Exception {
        return session.mData.serialise(mDataInfo).get();
    }

    public MDataInfo deserializeMdInfo(final byte[] serializedInfo) throws Exception {
        return session.mData.deserialise(serializedInfo).get();
    }

    public void reconnect() throws Exception {
        session.reconnect().get();
    }

    public void disconnect() throws Exception {
        session.testSimulateDisconnect().get();
    }
}
