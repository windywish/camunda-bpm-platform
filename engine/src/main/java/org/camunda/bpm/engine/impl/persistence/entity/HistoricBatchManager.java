/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.persistence.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.impl.Direction;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.QueryPropertyImpl;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.history.HistoricBatchEntity;
import org.camunda.bpm.engine.impl.batch.history.HistoricBatchQueryImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventProcessor;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.AbstractManager;
import org.camunda.bpm.engine.impl.util.ClockUtil;

public class HistoricBatchManager extends AbstractManager {

  public long findBatchCountByQueryCriteria(HistoricBatchQueryImpl historicBatchQuery) {
    configureQuery(historicBatchQuery);
    return (Long) getDbEntityManager().selectOne("selectHistoricBatchCountByQueryCriteria", historicBatchQuery);
  }

  @SuppressWarnings("unchecked")
  public List<HistoricBatch> findBatchesByQueryCriteria(HistoricBatchQueryImpl historicBatchQuery, Page page) {
    configureQuery(historicBatchQuery);
    return getDbEntityManager().selectList("selectHistoricBatchesByQueryCriteria", historicBatchQuery, page);
  }

  public HistoricBatchEntity findHistoricBatchById(String batchId) {
    return getDbEntityManager().selectById(HistoricBatchEntity.class, batchId);
  }

  @SuppressWarnings("unchecked")
  public List<String> findHistoricBatchIdsForCleanup(Integer batchSize, Map<String, Integer> batchOperationHistoryTimeToLiveMap) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("currentTimestamp", ClockUtil.getCurrentTime());
    map.put("map", batchOperationHistoryTimeToLiveMap);

    ListQueryParameterObject parameterObject = new ListQueryParameterObject();
    parameterObject.setParameter(map);
    parameterObject.getOrderingProperties().add(new QueryOrderingProperty(new QueryPropertyImpl("END_TIME_"), Direction.ASCENDING));
    parameterObject.setFirstResult(0);
    parameterObject.setMaxResults(batchSize);

    return (List<String>) getDbEntityManager().selectList("selectHistoricBatchIdsForCleanup", parameterObject);
  }

  public void deleteHistoricBatchById(String id) {
    getDbEntityManager().delete(HistoricBatchEntity.class, "deleteHistoricBatchById", id);
  }

  public void deleteHistoricBatchByIds(List<String> historicBatchIds) {
    CommandContext commandContext = Context.getCommandContext();

    commandContext.getHistoricIncidentManager().deleteHistoricIncidentsByBatchId(historicBatchIds);
    commandContext.getHistoricJobLogManager().deleteHistoricJobLogByBatchIds(historicBatchIds);

    getDbEntityManager().deletePreserveOrder(HistoricJobLogEventEntity.class, "deleteHistoricBatchByIds", historicBatchIds);

  }

  public void createHistoricBatch(final BatchEntity batch) {
    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();

    HistoryLevel historyLevel = configuration.getHistoryLevel();
    if(historyLevel.isHistoryEventProduced(HistoryEventTypes.BATCH_START, batch)) {

      HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
        @Override
        public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
          return producer.createBatchStartEvent(batch);
        }
      });
    }
  }

  public void completeHistoricBatch(final BatchEntity batch) {
    ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();

    HistoryLevel historyLevel = configuration.getHistoryLevel();
    if(historyLevel.isHistoryEventProduced(HistoryEventTypes.BATCH_END, batch)) {

      HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
        @Override
        public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
          return producer.createBatchEndEvent(batch);
        }
      });
    }
  }

  protected void configureQuery(HistoricBatchQueryImpl query) {
    getAuthorizationManager().configureHistoricBatchQuery(query);
    getTenantManager().configureQuery(query);
  }

}
