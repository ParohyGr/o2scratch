package com.parohy.outwo

import com.parohy.outwo.core.*
import org.junit.jupiter.api.Test
import kotlin.test.*

class CoreStateTest {
  @Test
  fun `given state is Loading, get isLoading should return true`() {
    val state = Loading
    assertTrue(state.isLoading, "isLoading should return true")
  }

  @Test
  fun `given state is not Loading, get isLoading should return false`() {
    val state = Failure("Error")
    assertFalse(state.isLoading, "isLoading should return false")
  }

  @Test
  fun `given state is Content, get isContent should return true`() {
    val state = Content("Success")
    assertTrue(state.isContent, "isContent should return true")
  }

  @Test
  fun `given state is Content, get valueOrNull should return value`() {
    val value = "Success"
    val state = Content(value)
    assertTrue(state.valueOrNull != null, "valueOrNull should return $value")
    assertEquals(value, state.valueOrNull)
  }

  @Test
  fun `given state is not Content, get isContent should return false`() {
    val state = Loading
    assertFalse(state.isContent, "isContent should return false")
  }

  @Test
  fun `given state is Failure, get isFailure should return true`() {
    val state = Failure("Success")
    assertTrue(state.isFailure, "isFailure should return true")
  }

  @Test
  fun `given state is not Failure, get isFailure should return false`() {
    val state = Loading
    assertFalse(state.isFailure, "isFailure should return false")
  }

  @Test
  fun `given state is Failure, get failureOrNull should return failure`() {
    val value = "Error"
    val state = Failure(value)
    assertTrue(state.failureOrNull != null, "failureOrNull should return $value")
    assertEquals(value, state.failureOrNull)
  }

  @Test
  fun `given result is Result isSuccess, toState should return Content`() {
    val value = "Success"
    val result = Result.success(value)
    val state = result.toState()
    assertTrue(state.isContent, "toState should return Content")
    assertEquals(value, state.valueOrNull)
  }

  @Test
  fun `given result is Result isFailure, toState should return Content`() {
    val value = "Error"
    val result = Result.failure<String>(Throwable(value))
    val state = result.toState()
    assertTrue(state.isFailure, "toState should return Failure")
    assertEquals(value, state.failureOrNull?.message)
  }
}