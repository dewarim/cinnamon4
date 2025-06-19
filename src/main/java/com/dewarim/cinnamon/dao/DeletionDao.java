package com.dewarim.cinnamon.dao;

import com.dewarim.cinnamon.model.Deletion;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public class DeletionDao implements CrudDao<Deletion>{

    @Override
    public String getTypeClassName() {
        return Deletion.class.getName();
    }

    public List<Deletion> listPendingDeletions(){
        SqlSession sqlSession = getSqlSession();
        return sqlSession.selectList("listPendingDeletions");
    }
}
