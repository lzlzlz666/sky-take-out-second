package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    SetmealVO getSetmealById(Long id);

    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void save(SetmealDTO setmealDTO);

    void startOrStop(Integer status, Long id);

    void updateSetmealWithsetmealDishes(SetmealDTO setmealDTO);

    void deleteBatchByIds(List<Long> ids);
}
