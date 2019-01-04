package net.maidsafe.sample.services;

import android.content.Context;
import android.util.Log;

import net.maidsafe.api.Client;
import net.maidsafe.api.Constants;
import net.maidsafe.api.Session;
import net.maidsafe.api.listener.OnDisconnected;
import net.maidsafe.api.model.AuthResponse;
import net.maidsafe.api.model.DecodeResult;
import net.maidsafe.api.model.NativeHandle;
import net.maidsafe.safe_app.MDataEntry;
import net.maidsafe.safe_app.MDataInfo;
import net.maidsafe.safe_app.MDataValue;
import net.maidsafe.safe_app.PermissionSet;

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
        this.appId = applicationId;
        final DecodeResult decodeResult = Session.decodeIpcMessage(response).get();
        if (decodeResult.getClass().equals(AuthResponse.class)) {
            final AuthResponse authResponse = (AuthResponse) decodeResult;
            session = Client.connect(applicationId, authResponse.getAuthGranted()).get();
            session.setOnDisconnectListener(onDisconnected);
            Log.i("STAGE:", "Connected to the SAFE Network");
        } else {
            throw new java.lang.Exception("Could not connect to the SAFE Network");
        }
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
        final NativeHandle entryHandle = session.mData.getEntriesHandle(mDataInfo).get();
        return session.mDataEntries.listEntries(entryHandle).get();
    }

    public void insertPermissions(final MDataInfo mDataInfo) throws Exception {
        final PermissionSet permissionSet = new PermissionSet();
        permissionSet.setInsert(true);
        permissionSet.setUpdate(true);
        permissionSet.setRead(true);
        permissionSet.setDelete(true);
        final NativeHandle permissionHandle = session.mDataPermission.newPermissionHandle().get();
        session.mDataPermission.insert(permissionHandle, session.crypto.getAppPublicSignKey().get(),
                permissionSet).get();
        session.mData.put(mDataInfo, permissionHandle, Constants.MD_ENTRIES_EMPTY).get();
        Log.i("STAGE:", "MData PUT is complete");
    }

    public void addEntry(final byte[] key, final byte[] value, final MDataInfo mDataInfo) throws Exception {
        final byte[] encryptedKey = session.mData.encryptEntryKey(mDataInfo, key).get();
        final byte[] encryptedValue = session.mData.encryptEntryValue(mDataInfo, value).get();
        final NativeHandle actionHandle = session.mDataEntryAction.newEntryAction().get();
        session.mDataEntryAction.insert(actionHandle, encryptedKey, encryptedValue).get();
        session.mData.mutateEntries(mDataInfo, actionHandle).get();
    }

    public void deleteEntry(final byte[] key, final long version, final MDataInfo mDataInfo) throws Exception {
        final byte[] encryptedKey = session.mData.encryptEntryKey(mDataInfo, key).get();
        final NativeHandle actionHandle = session.mDataEntryAction.newEntryAction().get();
        session.mDataEntryAction.delete(actionHandle, encryptedKey, version).get();
        session.mData.mutateEntries(mDataInfo, actionHandle).get();
    }

    public void updateEntry(final byte[] key, final byte[] newValue, final long version,
                            final MDataInfo mDataInfo) throws Exception {
        final byte[] encryptedKey = session.mData.encryptEntryKey(mDataInfo, key).get();
        final byte[] encryptedValue = session.mData.encryptEntryValue(mDataInfo, newValue).get();
        final NativeHandle actionHandle = session.mDataEntryAction.newEntryAction().get();
        session.mDataEntryAction.update(actionHandle, encryptedKey, encryptedValue, version).get();
        session.mData.mutateEntries(mDataInfo, actionHandle).get();
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
        return session.mData.getRandomPrivateMData(tagType).get();
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
        return session.mData.decrypt(mDataInfo, mDataEntry.getValue().getContent()).get();
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
