package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDTO
     */
    @Transactional
    public void save(DishDTO dishDTO) {
        // 新增1个Dish信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        dishMapper.insert(dish);

        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();

        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavor ->{
                dishFlavor.setDishId(dishId);
            });
            // 新增n个Flavor信息
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品信息分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据ids批量删除菜品
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 起售中的菜品不能删除(过滤出未售的商品)
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 被套餐关联的菜品不能删除
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }

        // 删除菜品后，关联的口味数据也需要删除掉
        /*for (Long id : ids) {
            dishMapper.deleteById(id);
            dishFlavorMapper.deletByDishId(id);
        }*/
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 根据id查询商品和风味
     * @param id
     */
    public DishVO getDishByIdwithFlavor(Long id) {
        Dish dish = dishMapper.getById(id);
        List<DishFlavor> dishFlavorList = dishFlavorMapper.getByDishId(id);

        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish , dishVO);
        dishVO.setFlavors(dishFlavorList);

        return dishVO;
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Transactional
    public void update(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO , dish);

        dishMapper.update(dish);

        dishFlavorMapper.deletByDishId(dishDTO.getId());

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavor ->{
                dishFlavor.setDishId(dishDTO.getId());
            });
            // 新增n个Flavor信息
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /**
     * 菜品的起售与停售修改
     * @param status
     * @param id
     */
    @Transactional
    public void updateStatus(Integer status, Long id) {
        // 当菜品停售时候该套餐也要停售
        if (status == StatusConstant.DISABLE) {
            setmealMapper.disableSetmealByDishId(
                    id,
                    StatusConstant.DISABLE,
                    BaseContext.getCurrentId()
            );

        }


        Dish dish = Dish.builder()
                    .status(status)
                    .id(id)
                    .build();
        dishMapper.updateSaleOrNotSale(dish);
    }

    /**
     * 根据分类id查询商品
     * @param categoryId
     * @return
     */
    public List<Dish> getDsihByCategoryId(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
