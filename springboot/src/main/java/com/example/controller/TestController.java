package com.example.controller;

import com.example.service.ICarouselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    ICarouselService iCarouselService;
    @RequestMapping("/hello")
    public String hello(){
        return "hello world";
    }
    @RequestMapping("/carousel")
    public String carousel(){
        return iCarouselService.getById(1).getPictureurl();
    }
}
