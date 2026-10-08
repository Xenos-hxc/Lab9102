package com.example.service.impl;

import com.example.entity.News;
import com.example.mapper.NewsMapper;
import com.example.service.INewsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author example.demo
 * @since 2025-11-04
 */
@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News> implements INewsService {

}
