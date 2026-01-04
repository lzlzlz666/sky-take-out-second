package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    SetmealVO getSetmealById(Long id);

    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void save(SetmealDTO setmealDTO);

    void startOrStop(Integer status, Long id);

    void updateSetmealWithsetmealDishes(SetmealDTO setmealDTO);

    void deleteBatchByIds(List<Long> ids);

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);
}
