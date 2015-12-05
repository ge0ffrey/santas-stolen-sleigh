/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package org.optaplannerdelirium.sss.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.impl.score.buildin.hardsoftlong.HardSoftLongScoreDefinition;
import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplanner.persistence.xstream.impl.score.XStreamScoreConverter;

@PlanningSolution
@XStreamAlias("ReindeerRoutingSolution")
public class ReindeerRoutingSolution extends AbstractPersistable implements Solution<HardSoftLongScore> {

    protected List<Gift> giftList;
    protected List<Reindeer> reindeerList;

    protected List<GiftAssignment> giftAssignmentList;

    @XStreamConverter(value = XStreamScoreConverter.class, types = {HardSoftLongScoreDefinition.class})
    protected HardSoftLongScore score;

    public List<Gift> getGiftList() {
        return giftList;
    }

    public void setGiftList(List<Gift> giftList) {
        this.giftList = giftList;
    }

    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "reindeerRange")
    public List<Reindeer> getReindeerList() {
        return reindeerList;
    }

    public void setReindeerList(List<Reindeer> reindeerList) {
        this.reindeerList = reindeerList;
    }

    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "giftAssignmentRange")
    public List<GiftAssignment> getGiftAssignmentList() {
        return giftAssignmentList;
    }

    public void setGiftAssignmentList(List<GiftAssignment> giftAssignmentList) {
        this.giftAssignmentList = giftAssignmentList;
    }

    public HardSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardSoftLongScore score) {
        this.score = score;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public Collection<? extends Object> getProblemFacts() {
        List<Object> facts = new ArrayList<Object>();
        facts.addAll(giftList);
        // Do not add the planning entities (reindeerList, giftAssignmentList) because that will be done automatically
        return facts;
    }

}
