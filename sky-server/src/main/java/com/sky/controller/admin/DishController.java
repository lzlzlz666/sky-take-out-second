package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        dishService.save(dishDTO);

        String name = "dish_" + dishDTO.getCategoryId();
        cleanCache(name);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result deleteBatch(@RequestParam List<Long> ids) {
        dishService.deleteBatch(ids);

        cleanCache("dish_*");
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品和对应的风味")
    public Result<DishVO> getDishByIdwithFlavor(@PathVariable Long id){
        DishVO dishVOList = dishService.getDishByIdwithFlavor(id);
        return Result.success(dishVOList);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result updateDish(@RequestBody DishDTO dishDTO) {
        dishService.update(dishDTO);

        cleanCache("dish_*");
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售、停售")
    public Result updateStatus(@PathVariable Integer status, Long id) {
        dishService.updateStatus(status, id);

        cleanCache("dish_*");
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询商品")
    public Result<List<Dish>> getDishByCatogoryId(@RequestParam Long categoryId) {
        List<Dish> dishList = dishService.getDsihByCategoryId(categoryId);
        return Result.success(dishList);
    }

    /**
     * 清理缓存数据
     * @param pattern
     */
    private void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
