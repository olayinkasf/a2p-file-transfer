package desktop.olayinka.file.transfer.model;

import com.olayinka.file.transfer.model.Device;
import com.olayinka.file.transfer.model.DeviceProvider;
import com.olayinka.file.transfer.model.Transfer;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Created by Olayinka on 11/2/2015.
 */
public class JDBCDeviceProvider implements DeviceProvider {


    private final EntityManager mManager;

    public JDBCDeviceProvider(EntityManager manager) {
        this.mManager = manager;
    }

    @Override
    public Device findDeviceById(Long id) {
        try {
            return mManager.find(Device.class, id);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    @Override
    public Device findDeviceByMacAddress(String macAddress) {
        try {
            return (Device) mManager.createQuery("SELECT device FROM Device as device where device.macAddress = :macAddress")
                    .setParameter("macAddress", macAddress).getSingleResult();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    @Override
    public void loadDeviceTransfers(Device device) {
        try {
            device.setTransfers(
                    (List<Transfer>) mManager.createQuery("SELECT transfer FROM Transfer as transfer where transfer.deviceId = :deviceId")
                            .setParameter("deviceId", device.getId()).getResultList()
            );
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public boolean deleteDevice(Device device) {

        Device tmpDevice = findDeviceByMacAddress(device.getMacAddress());
        if (tmpDevice == null) return false;

        device.merge(tmpDevice);

        mManager.getTransaction().begin();
        try {
            device.setStatus(Device.Status.BANNED);
            device.prePersist();
            mManager.merge(device);
        } catch (Throwable throwable) {
            mManager.getTransaction().rollback();
            throwable.printStackTrace();
            return false;
        }
        mManager.flush();
        mManager.getTransaction().commit();
        return true;
    }

    @Override
    public boolean insertDevice(Device device) {

        if (device.getId() != null && device.getId() > 0) return false;
        Device tmpDevice = findDeviceByMacAddress(device.getMacAddress());
        if (tmpDevice != null) return false;

        mManager.getTransaction().begin();
        try {
            //IMPORTANT
            device.setId(null);
            device.prePersist();
            mManager.persist(device);
        } catch (Throwable throwable) {
            mManager.getTransaction().rollback();
            throwable.printStackTrace();
            return false;
        }

        mManager.flush();
        mManager.getTransaction().commit();
        return true;
    }

    @Override
    public boolean updateDevice(Device device) {

        Device tmpDevice = findDeviceByMacAddress(device.getMacAddress());
        if (tmpDevice == null) return false;

        device.merge(tmpDevice);

        mManager.getTransaction().begin();
        try {
            device.prePersist();
            mManager.merge(device);
        } catch (Throwable throwable) {
            mManager.getTransaction().rollback();
            throwable.printStackTrace();
            return false;
        }

        mManager.flush();
        mManager.getTransaction().commit();
        return true;
    }

}
