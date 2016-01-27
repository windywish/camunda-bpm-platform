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
package org.camunda.bpm.engine.impl.migration;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationInstruction {

  protected String sourceActivityId;
  protected String targetActivityId;



  public MigrationInstruction(String sourceActivityId, String targetActivityId) {
    this.sourceActivityId = sourceActivityId;
    this.targetActivityId = targetActivityId;
  }

  public String getSourceActivityId() {
    return sourceActivityId;
  }

  public void setSourceActivityId(String sourceActivityId) {
    this.sourceActivityId = sourceActivityId;
  }

  public String getTargetActivityId() {
    return targetActivityId;
  }

  public void setTargetActivityId(String targetActivityId) {
    this.targetActivityId = targetActivityId;
  }



}