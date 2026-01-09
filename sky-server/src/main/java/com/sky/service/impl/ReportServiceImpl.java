package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.models.auth.In;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额统计
     *
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        ArrayList<LocalDate> dateList = new ArrayList<>();
        // 将从begin到end每一天日期存入localDateArrayList集合中
        while (!begin.isEqual(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        dateList.add(end);

        ArrayList<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0 : turnover;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 用户统计接口
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        ArrayList<LocalDate> dateList = new ArrayList<>();
        // 将从begin到end每一天日期存入localDateArrayList集合中
        while (!begin.isEqual(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        dateList.add(end);

        ArrayList<Long> totalUserList = new ArrayList<>();
        ArrayList<Long> newUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("end", endTime);
            Long totalUser = userMapper.countUserByMap(map);

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            map.put("begin", beginTime);
            Long newUser = userMapper.countUserByMap(map);

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .build();
    }

    /**
     * 订单统计接口
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        ArrayList<LocalDate> dateList = new ArrayList<>();
        // 将从begin到end每一天日期存入localDateArrayList集合中
        while (!begin.isEqual(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        dateList.add(end);

        ArrayList<Integer> totalOrderCountList = new ArrayList<>();
        ArrayList<Integer> validOrderCountList = new ArrayList<>();
        Integer totalOrders = 0; // 订单总数
        Integer validOrders = 0; // 有效订单数
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            Integer totalOrder = orderMapper.countAll(map);
            totalOrderCountList.add(totalOrder);

            map.put("status", Orders.COMPLETED);
            Integer validOrder = orderMapper.countAll(map);
            validOrderCountList.add(validOrder);
            totalOrders += totalOrder;
            validOrders += validOrder;
        }
        Double orderCompletionRate = 0.0;

        if (totalOrders > 0) {
            orderCompletionRate = validOrders * 1.0 / totalOrders;
        }


        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(totalOrderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .totalOrderCount(totalOrders)
                .validOrderCount(validOrders)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 销量前10接口
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO getSaleTop10(LocalDate begin, LocalDate end) {
        // 1. 处理时间区间：begin 变为当天的 00:00:00，end 变为当天的 23:59:59
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 2. 从数据库查询销量前10的数据，返回 DTO 列表
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);

        // 3. 通过 Stream 流提取商品名称(Name)，并使用 StringUtils 拼接成逗号分隔的字符串
        // 结果示例: "宫保鸡丁,麻婆豆腐,鱼香肉丝"
        List<String> names = salesTop10.stream()
                .map(GoodsSalesDTO::getName)
                .collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");

        // 4. 通过 Stream 流提取商品销量(Number)，并拼接成字符串
        // 结果示例: "300,250,180"
        List<Integer> numbers = salesTop10.stream()
                .map(GoodsSalesDTO::getNumber)
                .collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");

        // 5. 封装成 VO 对象返回
        return SalesTop10ReportVO
                .builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }
}
