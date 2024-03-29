/**
 * Copyright (c) 2016 Evernote Corporation. All rights reserved.
 */
package com.evernote.android.data.test;


import android.database.Cursor;

import com.evernote.android.data.Converter;
import com.evernote.android.data.Fetcher;
import com.evernote.android.data.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FetcherTest {

  @Mock
  Cursor cursor;

  @Test
  public void nullCursor() {
    Fetcher fetcher = Fetcher.of(null);
    assertThat(fetcher.toList(Converter.STRING)).isNotNull().isEmpty();
    assertThat(fetcher.toSet(Converter.STRING)).isNotNull().isEmpty();
    assertThat(fetcher.toCollection(Converter.STRING, Collections.<String>emptyList()))
        .isNotNull().isEmpty();
    final Optional<String> result = fetcher.toValue(Converter.STRING);
    assertThat(result).isNotNull();
    assertFalse(result.isPresent());
  }

  @Test
  public void emptyCursorToList() {
    when(cursor.getCount()).thenReturn(0);
    Fetcher fetcher = Fetcher.of(cursor);
    List<String> result = fetcher.toList(Converter.STRING);
    assertThat(result).isNotNull().isEmpty();
    verify(cursor).close();
  }

  @Test
  public void emptyCursorToValue() {
    when(cursor.getCount()).thenReturn(0);
    Fetcher fetcher = Fetcher.of(cursor);
    Optional<String> result = fetcher.toValue(Converter.STRING);
    assertFalse(result.isPresent());
    verify(cursor).close();
  }

  @Test
  public void singleValueToValue() {
    when(cursor.getCount()).thenReturn(1);
    when(cursor.moveToFirst()).thenReturn(true);
    when(cursor.getInt(0)).thenReturn(42);
    Fetcher fetcher = Fetcher.of(cursor);
    Optional<Integer> result = fetcher.toValue(Converter.INT);
    assertTrue("We should have a value", result.isPresent());
    assertThat(result.get()).isEqualTo(42);
  }

  @Test
  public void singleNullValue() {
    when(cursor.getCount()).thenReturn(1);
    when(cursor.moveToFirst()).thenReturn(true);
    Fetcher fetcher = Fetcher.of(cursor);
    Optional<String> result = fetcher.toValue(Converter.STRING);
    assertFalse("We should not have a value", result.isPresent());
  }

  @Test
  public void singleValueToList() {
    when(cursor.getCount()).thenReturn(1);
    when(cursor.moveToNext()).thenReturn(true, false);
    when(cursor.getString(0)).thenReturn("Yo");
    Fetcher fetcher = Fetcher.of(cursor);
    List<String> result = fetcher.toList(Converter.STRING);
    assertThat(result).isNotNull().hasSize(1).contains("Yo");
  }

  @Test
  public void singleNullValueToList() {
    when(cursor.getCount()).thenReturn(1);
    when(cursor.moveToNext()).thenReturn(true, false);
    when(cursor.getString(0)).thenReturn(null);
    Fetcher fetcher = Fetcher.of(cursor);
    List<String> result = fetcher.toList(Converter.STRING);
    assertThat(result).isNotNull().hasSize(1).contains((String) null);
  }

  @Test
  public void skipNulls() {
    when(cursor.getCount()).thenReturn(3);
    when(cursor.moveToNext()).thenReturn(true, true, true, false);
    when(cursor.getString(0)).thenReturn("First", null, "Third");
    Fetcher fetcher = Fetcher.of(cursor).skipNulls();
    List<String> result = fetcher.toList(Converter.STRING);
    assertThat(result).isNotNull().hasSize(2)
        .contains("First", "Third")
        .doesNotContain((String) null);
  }

  @Test
  public void countEmpty() {
    assertThat(Fetcher.of(null).count()).isEqualTo(0);
    assertThat(Fetcher.of(cursor).count()).isEqualTo(0);
    verify(cursor).close();
  }

  @Test
  public void countNonEmpty() {
    when(cursor.getCount()).thenReturn(42);
    assertThat(Fetcher.of(cursor).count()).isEqualTo(42);
    verify(cursor).close();
  }

}
