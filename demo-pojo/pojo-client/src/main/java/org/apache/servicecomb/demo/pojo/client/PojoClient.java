/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.demo.pojo.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import org.apache.servicecomb.core.Const;
import org.apache.servicecomb.core.CseContext;
import org.apache.servicecomb.core.provider.consumer.InvokerUtils;
import org.apache.servicecomb.demo.DemoConst;
import org.apache.servicecomb.demo.TestMgr;
import org.apache.servicecomb.demo.server.Test;
import org.apache.servicecomb.demo.server.TestRequest;
import org.apache.servicecomb.demo.smartcare.Application;
import org.apache.servicecomb.demo.smartcare.Group;
import org.apache.servicecomb.demo.smartcare.SmartCare;
import org.apache.servicecomb.foundation.common.utils.BeanUtils;
import org.apache.servicecomb.foundation.common.utils.Log4jUtils;
import org.apache.servicecomb.provider.pojo.RpcReference;
import org.apache.servicecomb.swagger.invocation.context.ContextUtils;
import org.apache.servicecomb.swagger.invocation.context.InvocationContext;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import benchmark.bean.Page;
import benchmark.bean.User;
import benchmark.rpc.AbstractClient;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import benchmark.service.UserService;

//@State
//State 用于声明某个类是一个“状态”，然后接受一个 Scope 参数用来表示该状态的共享范围。因为很多 benchmark 会需要一些表示状态的类，JMH 允许你把这些类以依赖注入的方式注入到 benchmark 函数里。Scope 主要分为两种。
//
//        (1).Thread: 该状态为每个线程独享。
//        (2).Benchmark: 该状态在所有线程间共享
@State(Scope.Benchmark)
@Component
public class PojoClient extends AbstractClient {


//  @BenchmarkMode(Mode.Throughput)//基准测试类型
//  @OutputTimeUnit(TimeUnit.SECONDS)//基准测试结果的时间类型
//  @Warmup(iterations = 3)//预热的迭代次数
//  @Threads(2)//测试线程数量
//  @State(Scope.Thread)//该状态为每个线程独享
////度量:iterations进行测试的轮次，time每轮进行的时长，timeUnit时长单位,batchSize批次数量
//  @Measurement(iterations = 2, time = -1, timeUnit = TimeUnit.SECONDS, batchSize = -1)
//
  public static final int CONCURRENCY = 32; //测试线程数量
  public static final int Threads = 32; //测试线程数量

  @RpcReference(microserviceName = "codefirst", schemaId="codeFirstHello")
  private static UserService userService;

  @Override
  protected UserService getUserService() {
    return userService;
  }

  static {
    try {
      Log4jUtils.init();
    } catch (Exception e) {
      e.printStackTrace();
    }
    BeanUtils.init();
  }

  @Benchmark
  @BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Override
  public boolean existUser() throws Exception {
    return super.existUser();
  }


  @Benchmark
  @BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Override
  public boolean createUser() throws Exception {
    return super.createUser();
  }

  @Benchmark
  @BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Override
  public User getUser() throws Exception {
    return super.getUser();
  }

  @Benchmark
  @BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Override
  public Page<User> listUser() throws Exception {
    return super.listUser();
  }

  public static void main(String[] args) throws Exception {
//    Log4jUtils.init();
//    BeanUtils.init();
    PojoClient client = new PojoClient();

    for (int i = 0; i < 60; i++) {
      try {
        System.out.println(client.getUser());
        break;
      } catch (Exception e) {
        Thread.sleep(1000);
      }
    }

    Options opt = new OptionsBuilder()//
            .include(PojoClient.class.getSimpleName())//
            .warmupIterations(10)// 预热的迭代次数  10 //number of times the warmup iteration should take place
            .measurementIterations(3)// 测试迭代 3    //number of times the actual iteration
            .threads(CONCURRENCY)// 线程数 32
            .forks(1)//
            .build();

    new Runner(opt).run();
  }
}
//  p