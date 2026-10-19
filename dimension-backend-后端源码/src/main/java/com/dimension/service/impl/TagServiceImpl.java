package com.dimension.service.impl;

import com.dimension.entity.Tag;
import com.dimension.mapper.TagMapper;
import com.dimension.service.TagService;
import com.dimension.utils.SessionUtil;

import java.util.List;

public class TagServiceImpl implements TagService {
    private TagMapper tagMapper = SessionUtil.getMapper(TagMapper.class);

    @Override
    public List<Tag> getAllTag() {
        List<Tag> list = tagMapper.getAllTag();
        return list;
    }
}
