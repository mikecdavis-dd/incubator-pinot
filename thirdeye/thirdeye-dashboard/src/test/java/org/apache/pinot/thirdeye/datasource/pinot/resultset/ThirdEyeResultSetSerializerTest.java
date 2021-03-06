/**
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pinot.thirdeye.datasource.pinot.resultset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.pinot.thirdeye.dataframe.DataFrame;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ThirdEyeResultSetSerializerTest {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @BeforeClass
  public void initObjectMapper() {
    SimpleModule module = new SimpleModule("ThirdEyeResultSetSerializer", new Version(1, 0, 0, null, null, null));
    module.addSerializer(ThirdEyeResultSet.class, new ThirdEyeResultSetSerializer());
    OBJECT_MAPPER.registerModule(module);
  }

  @Test
  public void testSerializeSelectResult() {
    List<String> columnArray = new ArrayList<>();
    columnArray.add("col1");
    columnArray.add("col2");

    DataFrame dataFrame = new DataFrame();
    dataFrame.addSeries("col1", 0, 10, 20);
    dataFrame.addSeries("col2", 1, 11, 21);
    ThirdEyeResultSetMetaData metaData = new ThirdEyeResultSetMetaData(Collections.<String>emptyList(), columnArray);
    ThirdEyeResultSet thirdeyeResultSet = new ThirdEyeDataFrameResultSet(metaData, dataFrame);

    String actualJsonString = toJsonString(thirdeyeResultSet);

    final String expectedJsonString = "{\"" + ThirdEyeResultSetSerializer.GROUP_COLUMN_NAMES_FIELD + "\":[],\""
        + ThirdEyeResultSetSerializer.METRIC_COLUMN_NAMES_FIELD + "\":[\"col1\",\"col2\"],\""
        + ThirdEyeResultSetSerializer.ROWS_FIELD + "\":[[\"0\",\"1\"],[\"10\",\"11\"],[\"20\",\"21\"]]}";

    Assert.assertEquals(actualJsonString, expectedJsonString);
  }

  @Test
  public void testSerializeAggregationResult() {
    final String functionName = "sum_metric_name";
    ThirdEyeResultSetMetaData metaData =
        new ThirdEyeResultSetMetaData(Collections.<String>emptyList(), Collections.singletonList(functionName));
    DataFrame dataFrame = new DataFrame();
    dataFrame.addSeries(functionName, 150.33576);
    ThirdEyeResultSet thirdeyeResultSet = new ThirdEyeDataFrameResultSet(metaData, dataFrame);

    String actualJsonString = toJsonString(thirdeyeResultSet);

    final String expectedJsonString = "{\"" + ThirdEyeResultSetSerializer.GROUP_COLUMN_NAMES_FIELD + "\":[],\""
        + ThirdEyeResultSetSerializer.METRIC_COLUMN_NAMES_FIELD + "\":[\"sum_metric_name\"],\""
        + ThirdEyeResultSetSerializer.ROWS_FIELD + "\":[[\"150.33576\"]]}";

    Assert.assertEquals(actualJsonString, expectedJsonString);
  }

  @Test
  public void testSerializeGroupByResult() {
    final String functionName = "sum_metric_name";

    List<String> groupByColumnNames = new ArrayList<>();
    groupByColumnNames.add("country");
    groupByColumnNames.add("pageName");

    ThirdEyeResultSetMetaData metaData =
        new ThirdEyeResultSetMetaData(groupByColumnNames, Collections.singletonList(functionName));
    DataFrame dataFrame = new DataFrame();
    dataFrame.addSeries("country", "US", "US", "IN", "JP");
    dataFrame.addSeries("pageName", "page1", "page2", "page3", "page2");
    dataFrame.addSeries(functionName, 1111, 2222.2, 333.3, 44444.4);
    ThirdEyeResultSet thirdeyeResultSet = new ThirdEyeDataFrameResultSet(metaData, dataFrame);

    String actualJsonString = toJsonString(thirdeyeResultSet);

    final String expectedJsonString =
        "{\"" + ThirdEyeResultSetSerializer.GROUP_COLUMN_NAMES_FIELD + "\":[\"country\",\"pageName\"],\""
            + ThirdEyeResultSetSerializer.METRIC_COLUMN_NAMES_FIELD + "\":[\"sum_metric_name\"],\""
            + ThirdEyeResultSetSerializer.ROWS_FIELD
            + "\":[[\"US\",\"page1\",\"1111.0\"],[\"US\",\"page2\",\"2222.2\"],[\"IN\",\"page3\",\"333.3\"],[\"JP\",\"page2\",\"44444.4\"]]}";

    Assert.assertEquals(actualJsonString, expectedJsonString);
  }

  private String toJsonString(ThirdEyeResultSet dataFrameResultSet) {
    try {
      return OBJECT_MAPPER.writeValueAsString(dataFrameResultSet);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return null;
    }
  }
}
