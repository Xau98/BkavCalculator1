#
# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := guava libarity_bkav

LOCAL_STATIC_ANDROID_LIBRARIES:= \
        android-support-v4 \
        android-support-compat \
        android-support-core-ui \
        android-support-v7-appcompat

LOCAL_USE_AAPT2 := true

LOCAL_SRC_FILES := $(call all-java-files-under, java)

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res_bkav \
    $(LOCAL_PATH)/res

LOCAL_PRIVATE_PLATFORM_APIS := true

LOCAL_AAPT_FLAGS := --auto-add-overlay

# Bkav QuangLH
LOCAL_PACKAGE_NAME := BkavCalculator
LOCAL_OVERRIDES_PACKAGES := Calculator ExactCalculator

include $(BUILD_PACKAGE)

##################################################
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libarity_bkav:../../libs/arity-2.1.7.jar

include $(BUILD_MULTI_PREBUILT)

# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
