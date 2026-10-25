package com.dimension.mapper;

import com.dimension.entity.Tag;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface TagMapper {
    //查询所有tag
    List<Tag> getAllTag();
    // 根据标签名查询标签（看是否已存在）
    Tag getTagByName(@Param("name") String name);

    // 插入新标签，并返回自增主键
    void insertTag(Tag tag);
}
