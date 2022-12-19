package ru.mralexeimk.yedom.utils.services;

import org.springframework.stereotype.Service;
import ru.mralexeimk.yedom.config.configs.BigFileConfig;
import ru.mralexeimk.yedom.database.entities.UserEntity;
import ru.mralexeimk.yedom.models.BigFile;
import ru.mralexeimk.yedom.models.User;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoaderService {
    private final BigFileConfig bigFileConfig;
    private final ConcurrentHashMap<Integer, BigFile> fileByUser = new ConcurrentHashMap<>();

    public LoaderService(BigFileConfig bigFileConfig) {
        this.bigFileConfig = bigFileConfig;
    }

    public void addFile(UserEntity userEntity, File file, byte[] bytes) {
        addFile(userEntity.getId(), file, bytes);
    }
    public void addFile(int userId, File file, byte[] bytes) {
        fileByUser.put(userId, new BigFile(file, bytes, bigFileConfig.getSizeOfPart()));
    }

    public void removeFile(UserEntity userEntity) {
        removeFile(userEntity.getId());
    }

    public void removeFile(int userId) {
        fileByUser.remove(userId);
    }

    public void startFileUploading(UserEntity userEntity) {
        startFileUploading(userEntity.getId());
    }

    public void startFileUploading(int userId) {
        new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(bigFileConfig.getUploadingPeriod());
                    if(fileByUser.get(userId).send() == null) {
                        removeFile(userId);
                        break;
                    }
                } catch (Exception ex) {
                    removeFile(userId);
                    break;
                }
            }
        }).start();
    }

    public double getProgress(UserEntity userEntity) {
        return getProgress(userEntity.getId());
    }

    public double getProgress(int userId) {
        try {
            return fileByUser.get(userId).getProgress();
        } catch (Exception ex) {
            return 100.0;
        }
    }
}
