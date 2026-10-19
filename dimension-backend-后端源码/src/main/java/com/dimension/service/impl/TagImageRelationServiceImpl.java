package com.dimension.service.impl;

import com.dimension.mapper.TagImageRelationMapper;
import com.dimension.service.TagImageRelationService;
import com.dimension.utils.SessionUtil;

public class TagImageRelationServiceImpl implements TagImageRelationService {
    private TagImageRelationMapper tagImageRelationMapper = SessionUtil.getMapper(TagImageRelationMapper.class);
}
