/*
 * Copyright (c) 2005-2011 Grameen Foundation USA
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
 */

package org.mifos.framework.util;

import static org.mifos.framework.util.CollectionUtils.asList;
import static org.mifos.framework.util.CollectionUtils.splitListIntoParts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.mifos.framework.util.helpers.Transformer;

@SuppressWarnings("unchecked")
public class CollectionUtilsTest extends TestCase {

    public void testAsListReturnsOneElementPassed() {
        List<Integer> list = asList(Integer.valueOf(0));
       assertEquals(1, list.size());
       assertEquals(Integer.valueOf(0), list.get(0));
    }

    public void testAsListReturnsListFormedOfMultipleElements() throws Exception {
        List<Integer> list = asList(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2));
       assertEquals(3, list.size());
       assertEquals(Integer.valueOf(0), list.get(0));
       assertEquals(Integer.valueOf(1), list.get(1));
       assertEquals(Integer.valueOf(2), list.get(2));
    }

    public void testSplitListReturnsEmptyListForEmptyList() throws Exception {
       assertEquals(Collections.EMPTY_LIST, splitListIntoParts(new ArrayList(), 10));
    }

    public void testSplitListThrowsExceptionIfSizeOfPartsIsZero() throws Exception {
        try {
            splitListIntoParts(Arrays.asList(1, 2, 3), 0);
            Assert.fail("Split list should throw exception if size of each part is zero");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testSplitReturnsSameListIfSizeOfEachPartIsGreaterThanListSize() throws Exception {
        List expected = new ArrayList();
        expected.add(Arrays.asList(1, 2, 3));
       assertEquals(expected, splitListIntoParts(Arrays.asList(1, 2, 3), 4));
    }

    public void testSplitListEvenlyIfListSizeIsMultipleOfSizeOfEachPart() throws Exception {
        List expected = new ArrayList();
        expected.add(Arrays.asList(1, 2));
        expected.add(Arrays.asList(3, 4));
       assertEquals(expected, splitListIntoParts(Arrays.asList(1, 2, 3, 4), 2));
    }

    public void testSplitReturnsOneSublistIfListSizeEqualsSizeOfEachPart() throws Exception {
        List expected = new ArrayList();
        expected.add(Arrays.asList(1, 2, 3));
       assertEquals(expected, splitListIntoParts(Arrays.asList(1, 2, 3), 3));
    }

    public void testSplitListIntoPartsAndARemainderIfListSizeIsNotMultipleOfSizeOfEachPart() throws Exception {
        List expected = new ArrayList();
        expected.add(Arrays.asList(1, 2));
        expected.add(Arrays.asList(3, 4));
        expected.add(Arrays.asList(5));
       assertEquals(expected, splitListIntoParts(Arrays.asList(1, 2, 3, 4, 5), 2));
    }

    public void testValuesAsMap() {
        Map<Integer,String> stringMap = CollectionUtils.asValueMap(asList("Two", "Three", "Four"), new Transformer<String, Integer>() {
            @Override
            public Integer transform(String input) {
                return input.length();
            }
        });
        assertEquals("Two", stringMap.get(3));
        assertEquals("Three", stringMap.get(5));
        assertEquals("Four", stringMap.get(4));
    }

}
