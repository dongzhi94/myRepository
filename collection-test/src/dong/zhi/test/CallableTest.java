package dong.zhi.test;

import java.math.BigDecimal;

import static java.math.BigDecimal.ROUND_HALF_UP;

public class CallableTest {

    public static void main(String[] args){
        BigDecimal projectIncome = new BigDecimal(2000);
        BigDecimal overallRate = null;
        Long perid = 64l;
        BigDecimal projectPeriod = new BigDecimal(perid);//项目期限
        System.out.println(projectPeriod);
        overallRate = projectIncome.divide(new BigDecimal(200000),10,ROUND_HALF_UP).divide(projectPeriod,10,ROUND_HALF_UP).multiply(new BigDecimal("365")).add(new BigDecimal("0.132")).setScale(4,ROUND_HALF_UP);
        System.out.println(overallRate);
    }
}
