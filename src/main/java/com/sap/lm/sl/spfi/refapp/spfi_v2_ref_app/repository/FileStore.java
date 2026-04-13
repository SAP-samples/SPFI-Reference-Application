package com.sap.lm.sl.spfi.refapp.spfi_v2_ref_app.repository;

import java.io.IOException;
import java.util.List;

public interface FileStore<T> {
    void store(T t, String fileName) throws IOException;

    T fetch(String fileName) throws IOException;

    void delete(String fileName) throws IOException;

    boolean exists(String fileName);

    List<T> fetchAll() throws IOException;


    String extendWithFileFormat(String fileName);
}
