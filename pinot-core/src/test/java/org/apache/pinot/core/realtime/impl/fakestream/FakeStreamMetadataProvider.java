/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pinot.core.realtime.impl.fakestream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;
import org.apache.pinot.spi.stream.OffsetCriteria;
import org.apache.pinot.spi.stream.PartitionGroupMetadata;
import org.apache.pinot.spi.stream.StreamConfig;
import org.apache.pinot.spi.stream.StreamMetadataProvider;
import org.apache.pinot.spi.stream.StreamPartitionMsgOffset;


/**
 * StreamMetadataProvider implementation for the fake stream
 */
public class FakeStreamMetadataProvider implements StreamMetadataProvider {
  private final int _numPartitions;

  public FakeStreamMetadataProvider(StreamConfig streamConfig) {
    _numPartitions = FakeStreamConfigUtils.getNumPartitions(streamConfig);
  }

  @Override
  public int fetchPartitionCount(long timeoutMillis) {
    return _numPartitions;
  }

  @Override
  public List<PartitionGroupMetadata> getPartitionGroupMetadataList(
      List<PartitionGroupMetadata> currentPartitionGroupsMetadata, long timeoutMillis) {
    List<PartitionGroupMetadata> partitionGroupMetadataList = new ArrayList<>();
    for (int i = 0; i < _numPartitions; i++) {
      partitionGroupMetadataList.add(new FakePartitionGroupMetadata(i));
    }
    return partitionGroupMetadataList;
  }

  public long fetchPartitionOffset(@Nonnull OffsetCriteria offsetCriteria, long timeoutMillis) throws TimeoutException {
    throw new UnsupportedOperationException("This method is deprecated");
  }

  @Override
  public StreamPartitionMsgOffset fetchStreamPartitionOffset(@Nonnull OffsetCriteria offsetCriteria, long timeoutMillis) throws TimeoutException {
    if (offsetCriteria.isSmallest()) {
      return FakeStreamConfigUtils.getSmallestOffset();
    } else {
      return FakeStreamConfigUtils.getLargestOffset();
    }
  }

  @Override
  public void close() throws IOException {

  }
}
