package com.dxc.application.databasestructure.database.mapper;

import com.dxc.application.databasestructure.model.DatabaseStructureModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DatabaseMetaData {
    List<String> getAllTableOfSchema(@Param("schemaName") String schemaName);
    List<DatabaseStructureModel> getColumnMetaByTable(@Param("schemaName") String schemaName,@Param("tableName") String tableName);
}
