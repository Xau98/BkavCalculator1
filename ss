[1mdiff --git a/.idea/caches/build_file_checksums.ser b/.idea/caches/build_file_checksums.ser[m
[1mindex 707a723..95df8e2 100644[m
Binary files a/.idea/caches/build_file_checksums.ser and b/.idea/caches/build_file_checksums.ser differ
[1mdiff --git a/.idea/gradle.xml b/.idea/gradle.xml[m
[1mindex d291b3d..674414f 100644[m
[1m--- a/.idea/gradle.xml[m
[1m+++ b/.idea/gradle.xml[m
[36m@@ -1,15 +1,20 @@[m
 <?xml version="1.0" encoding="UTF-8"?>[m
 <project version="4">[m
[32m+[m[32m  <component name="GradleMigrationSettings" migrationVersion="1" />[m
   <component name="GradleSettings">[m
     <option name="linkedExternalProjectsSettings">[m
       <GradleProjectSettings>[m
[31m-        <compositeConfiguration>[m
[31m-          <compositeBuild compositeDefinitionSource="SCRIPT" />[m
[31m-        </compositeConfiguration>[m
[32m+[m[32m        <option name="delegatedBuild" value="false" />[m
[32m+[m[32m        <option name="testRunner" value="PLATFORM" />[m
         <option name="distributionType" value="DEFAULT_WRAPPED" />[m
         <option name="externalProjectPath" value="$PROJECT_DIR$" />[m
[32m+[m[32m        <option name="modules">[m
[32m+[m[32m          <set>[m
[32m+[m[32m            <option value="$PROJECT_DIR$" />[m
[32m+[m[32m            <option value="$PROJECT_DIR$/app" />[m
[32m+[m[32m          </set>[m
[32m+[m[32m        </option>[m
         <option name="resolveModulePerSourceSet" value="false" />[m
[31m-        <option name="testRunner" value="PLATFORM" />[m
       </GradleProjectSettings>[m
     </option>[m
   </component>[m
[1mdiff --git a/.idea/modules.xml b/.idea/modules.xml[m
[1mindex 6396df0..f5652aa 100644[m
[1m--- a/.idea/modules.xml[m
[1m+++ b/.idea/modules.xml[m
[36m@@ -2,8 +2,8 @@[m
 <project version="4">[m
   <component name="ProjectModuleManager">[m
     <modules>[m
[31m-      <module fileurl="file://$PROJECT_DIR$/BkavCalculator.iml" filepath="$PROJECT_DIR$/BkavCalculator.iml" />[m
[31m-      <module fileurl="file://$PROJECT_DIR$/app/app.iml" filepath="$PROJECT_DIR$/app/app.iml" />[m
[32m+[m[32m      <module fileurl="file://$PROJECT_DIR$/BkavCalculator.iml" filepath="$PROJECT_DIR$/BkavCalculator.iml" group="BkavCalculator" />[m
[32m+[m[32m      <module fileurl="file://$PROJECT_DIR$/app/app.iml" filepath="$PROJECT_DIR$/app/app.iml" group="BkavCalculator/app" />[m
     </modules>[m
   </component>[m
 </project>[m
\ No newline at end of file[m
[1mdiff --git a/.idea/workspace.xml b/.idea/workspace.xml[m
[1mindex 8247a83..82d7a25 100644[m
[1m--- a/.idea/workspace.xml[m
[1m+++ b/.idea/workspace.xml[m
[36m@@ -6,29 +6,29 @@[m
     </shared>[m
   </component>[m
   <component name="AndroidLogFilters">[m
[31m-    <option name="TOOL_WINDOW_LOG_LEVEL" value="error" />[m
[32m+[m[32m    <option name="TOOL_WINDOW_LOG_LEVEL" value="verbose" />[m
     <option name="TOOL_WINDOW_CONFIGURED_FILTER" value="Show only selected application" />[m
     <option name="TOOL_WINDOW_REGEXP_FILTER" value="false" />[m
   </component>[m
[31m-  <component name="BookmarkManager">[m
[31m-    <bookmark url="file://$PROJECT_DIR$/app/src/main/java/com/android/calculator2/CalculatorResult.java" line="761" mnemonic="2" />[m
[31m-    <bookmark url="file://$PROJECT_DIR$/app/src/main/java/com/android/calculator2/Calculator.java" line="514" mnemonic="1" />[m
[31m-    <bookmark url="file://$PROJECT_DIR$/app/src/main/java/com/android/calculator2/Calculator.java" line="633" />[m
[31m-  </component>[m
   <component name="ChangeListManager">[m
     <list default="true" id="ac9bea0b-70d1-44fa-9642-a5f1677d6e9c" name="Default Changelist" comment="">[m
[31m-      <change afterPath="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/BkavMemoryFunction.java" afterDir="false" />[m
[32m+[m[32m      <change beforePath="$PROJECT_DIR$/.idea/caches/build_file_checksums.ser" beforeDir="false" afterPath="$PROJECT_DIR$/.idea/caches/build_file_checksums.ser" afterDir="false" />[m
[32m+[m[32m      <change beforePath="$PROJECT_DIR$/.idea/gradle.xml" beforeDir="false" afterPath="$PROJECT_DIR$/.idea/gradle.xml" afterDir="false" />[m
[32m+[m[32m      <change beforePath="$PROJECT_DIR$/.idea/modules.xml" beforeDir="false" afterPath="$PROJECT_DIR$/.idea/modules.xml" afterDir="false" />[m
       <change beforePath="$PROJECT_DIR$/.idea/workspace.xml" beforeDir="false" afterPath="$PROJECT_DIR$/.idea/workspace.xml" afterDir="false" />[m
[31m-      <change beforePath="$PROJECT_DIR$/app/build.gradle" beforeDir="false" afterPath="$PROJECT_DIR$/app/build.gradle" afterDir="false" />[m
[32m+[m[32m      <change beforePath="$PROJECT_DIR$/app/app.iml" beforeDir="false" afterPath="$PROJECT_DIR$/app/app.iml" afterDir="false" />[m
[32m+[m[32m      <change beforePath="$PROJECT_DIR$/app/src/main/AndroidManifest.xml" beforeDir="false" afterPath="$PROJECT_DIR$/app/src/main/AndroidManifest.xml" afterDir="false" />[m
[32m+[m[32m      <change beforePath="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/BkavAdvancedLayout.java" beforeDir="false" afterPath="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/BkavAdvancedLayout.java" afterDir="false" />[m
[32m+[m[32m      <change beforePath="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/BkavHistoryLayout.java" beforeDir="false" afterPath="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/BkavHistoryLayout.java" afterDir="false" />[m
       <change beforePath="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/Calculator.java" beforeDir="false" afterPath="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/Calculator.java" afterDir="false" />[m
[31m-      <change beforePath="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/CalculatorExpr.java" beforeDir="false" afterPath="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/CalculatorExpr.java" afterDir="false" />[m
       <change beforePath="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/CalculatorPadViewPager.java" beforeDir="false" afterPath="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/CalculatorPadViewPager.java" afterDir="false" />[m
[31m-      <change beforePath="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/Evaluator.java" beforeDir="false" afterPath="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/Evaluator.java" afterDir="false" />[m
[31m-      <change beforePath="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/HistoryFragment.java" beforeDir="false" afterPath="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/HistoryFragment.java" afterDir="false" />[m
[31m-      <change beforePath="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/KeyMaps.java" beforeDir="false" afterPath="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/KeyMaps.java" afterDir="false" />[m
[31m-      <change beforePath="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/UnifiedReal.java" beforeDir="false" afterPath="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/UnifiedReal.java" afterDir="false" />[m
[31m-      <change beforePath="$PROJECT_DIR$/app/src/main/res/layout/activity_calculator_port.xml" beforeDir="false" afterPath="$PROJECT_DIR$/app/src/main/res/layout/activity_calculator_port.xml" afterDir="false" />[m
[31m-      <change beforePath="$PROJECT_DIR$/build.gradle" beforeDir="false" afterPath="$PROJECT_DIR$/build.gradle" afterDir="false" />[m
[32m+[m[32m      <change beforePath="$PROJECT_DIR$/app/src/main/res/layout-land/display_two_line.xml" beforeDir="false" afterPath="$PROJECT_DIR$/app/src/main/res/layout-land/display_two_line.xml" afterDir="false" />[m
[32m+[m[32m      <change beforePath="$PROJECT_DIR$/app/src/main/res/layout/pad_advanced_4x4.xml" beforeDir="false" afterPath="$PROJECT_DIR$/app/src/main/res/layout/pad_advanced_4x4.xml" afterDir="false" />[m
[32m+[m[32m      <change beforePath="$PROJECT_DIR$/app/src/main/res/values-w230dp-h275dp/styles.xml" beforeDir="false" afterPath="$PROJECT_DIR$/app/src/main/res/values-w230dp-h275dp/styles.xml" afterDir="false" />[m
[32m+[m[32m      <change beforePath="$PROJECT_DIR$/app/src/main/res/values-w375dp-h500dp-port/dimens.xml" beforeDir="false" afterPath="$PROJECT_DIR$/app/src/main/res/values-w375dp-h500dp-port/dimens.xml" afterDir="false" />[m
[32m+[m[32m      <change beforePath="$PROJECT_DIR$/app/src/main/res/values-w375dp-h500dp-port/layout.xml" beforeDir="false" afterPath="$PROJECT_DIR$/app/src/main/res/values-w375dp-h500dp-port/layout.xml" afterDir="false" />[m
[32m+[m[32m      <change beforePath="$PROJECT_DIR$/app/src/main/res/values-w520dp-h275dp-land/styles.xml" beforeDir="false" afterPath="$PROJECT_DIR$/app/src/main/res/values-w520dp-h275dp-land/styles.xml" afterDir="false" />[m
[32m+[m[32m      <change beforePath="$PROJECT_DIR$/app/src/main/res/values-w520dp-h375dp-land/styles.xml" beforeDir="false" afterPath="$PROJECT_DIR$/app/src/main/res/values-w520dp-h375dp-land/styles.xml" afterDir="false" />[m
     </list>[m
     <option name="EXCLUDED_CONVERTED_TO_IGNORED" value="true" />[m
     <option name="SHOW_DIALOG" value="false" />[m
[36m@@ -36,10 +36,12 @@[m
     <option name="HIGHLIGHT_NON_ACTIVE_CHANGELIST" value="false" />[m
     <option name="LAST_RESOLUTION" value="IGNORE" />[m
   </component>[m
[31m-  <component name="DefaultGradleProjectSettings">[m
[31m-    <option name="isMigrated" value="true" />[m
[31m-  </component>[m
   <component name="ExecutionTargetManager" SELECTED_TARGET="1234567" />[m
[32m+[m[32m  <component name="ExternalProjectsData">[m
[32m+[m[32m    <projectState path="$PROJECT_DIR$">[m
[32m+[m[32m      <ProjectState />[m
[32m+[m[32m    </projectState>[m
[32m+[m[32m  </component>[m
   <component name="ExternalProjectsManager">[m
     <system id="GRADLE">[m
       <state>[m
[36m@@ -63,376 +65,20 @@[m
   <component name="FavoritesManager">[m
     <favorites_list name="BkavCalculator" />[m
   </component>[m
[31m-  <component name="FileEditorManager">[m
[31m-    <leaf SIDE_TABS_SIZE_LIMIT_KEY="300">[m
[31m-      <file pinned="false" current-in-tab="false">[m
[31m-        <entry file="file://$PROJECT_DIR$/app/src/main/java/com/android/calculator2/Calculator.java">[m
[31m-          <provider selected="true" editor-type-id="text-editor">[m
[31m-            <state relative-caret-position="103">[m
[31m-              <caret line="197" column="9" selection-start-line="197" selection-start-column="9" selection-end-line="197" selection-end-column="9" />[m
[31m-              <folding>[m
[31m-                <element signature="e#0#115599#0" expanded="true" />[m
[31m-                <element signature="imports" expanded="true" />[m
[31m-                <element signature="e#6841#6860#0" expanded="true" />[m
[31m-                <element signature="e#6971#6972#0" expanded="true" />[m
[31m-                <element signature="e#7048#7049#0" expanded="true" />[m
[31m-                <element signature="e#7942#8424#0" expanded="true" />[m
[31m-                <element signature="e#8423#8424#0" expanded="true" />[m
[31m-                <element signature="e#8573#8574#0" expanded="true" />[m
[31m-                <element signature="e#8632#8633#0" expanded="true" />[m
[31m-                <element signature="e#9075#9290#0" expanded="true" />[m
[31m-                <element signature="e#9289#9290#0" expanded="true" />[m
[31m-                <element signature="e#18052#18273#0" expanded="true" />[m
[31m-                <element signature="e#18272#18273#0" expanded="true" />[m
[31m-                <element signature="e#22014#22244#0" expanded="true" />[m
[31m-                <element signature="e#22243#22244#0" expanded="true" />[m
[31m-                <element signature="e#22352#25824#0" expanded="true" />[m
[31m-                <element signature="e#25823#25824#0" expanded="true" />[m
[31m-                <element signature="e#26661#28484#0" expanded="true" />[m
[31m-                <element signature="e#28483#28484#0" expanded="true" />[m
[31m-                <element signature="e#30621#31574#0" expanded="true" />[m
[31m-                <element signature="e#31573#31574#0" expanded="true" />[m
[31m-                <element signature="e#55616#55617#0" expanded="true" />[m
[31m-                <element signature="e#55649#55650#0" expanded="true" />[m
[31m-                <element signature="e#56282#56283#0" expanded="true" />[m
[31m-                <element signature="e#56327#56328#0" expanded="true" />[m
[31m-                <element signature="e#60561#60592#0" expanded="true" />[m
[31m-                <element signature="e#61347#61379#0" expanded="true" />[m
[31m-                <element signature="e#62434#62451#0" expanded="true" />[m
[31m-                <element signature="e#62498#62531#0" expanded="true" />[m
[31m-                <element signature="e#62567#62584#0" expanded="true" />[m
[31m-                <element signature="e#62633#62668#0" expanded="true" />[m
[31m-                <element signature="e#62718#62735#0" expanded="true" />[m
[31m-                <element signature="e#62782#62815#0" expanded="true" />[m
[31m-                <element signature="e#62851#62868#0" expanded="true" />[m
[31m-                <element signature="e#62917#62952#0" expanded="true" />[m
[31m-                <element signature="e#79011#79043#0" expanded="true" />[m
[31m-                <element signature="e#83975#83996#0" expanded="true" />[m
[31m-                <element signature="e#94490#94717#0" expanded="true" />[m
[31m-                <element signature="e#94716#94717#0" expanded="true" />[m
[31m-                <element signature="e#94895#94937#0" expanded="true" />[m
[31m-                <element signature="e#95447#95661#0" expanded="true" />[m
[31m-                <element signature="e#95660#95661#0" expanded="true" />[m
[31m-                <element signature="e#96220#96540#0" expanded="true" />[m
[31m-                <element signature="e#96539#96540#0" expanded="true" />[m
[31m-                <element signature="e#99532#99574#0" expanded="true" />[m
[31m-                <element signature="e#100517#100770#0" expanded="true" />[m
[31m-                <element signature="e#100769#100770#0" expanded="true" />[m
[31m-                <element signature="e#105294#105295#0" expanded="true" />[m
[31m-                <element signature="e#105333#105334#0" expanded="true" />[m
[31m-                <element signature="e#105963#105964#0" expanded="true" />[m
[31m-                <element signature="e#106018#106019#0" expanded="true" />[m
[31m-                <element signature="e#107900#107933#0" expanded="true" />[m
[31m-                <element signature="e#108283#108312#0" expanded="true" />[m
[31m-                <element signature="e#108350#108385#0" expanded="true" />[m
[31m-                <element signature="e#108420#108452#0" expanded="true" />[m
[31m-                <element signature="e#114945#114946#0" expanded="true" />[m
[31m-                <element signature="e#114990#114991#0" expanded="true" />[m
[31m-                <element signature="e#115161#115162#0" expanded="true" />[m
[31m-                <element signature="e#115209#115210#0" expanded="true" />[m
[31m-              </folding>[m
[31m-            </state>[m
[31m-          </provider>[m
[31m-        </entry>[m
[31m-      </file>[m
[31m-      <file pinned="false" current-in-tab="false">[m
[31m-        <entry file="file://$PROJECT_DIR$/app/src/main/java/com/android/calculator2/CalculatorExpr.java">[m
[31m-          <provider selected="true" editor-type-id="text-editor">[m
[31m-            <state relative-caret-position="195">[m
[31m-              <caret line="612" column="80" lean-forward="true" selection-start-line="612" selection-start-column="80" selection-end-line="612" selection-end-column="80" />[m
[31m-              <folding>[m
[31m-                <element signature="imports" expanded="true" />[m
[31m-                <element signature="e#4604#4605#0" expanded="true" />[m
[31m-                <element signature="e#4638#4639#0" expanded="true" />[m
[31m-              </folding>[m
[31m-            </state>[m
[31m-          </provider>[m
[31m-        </entry>[m
[31m-      </file>[m
[31m-      <file pinned="false" current-in-tab="false">[m
[31m-        <entry file="file://$PROJECT_DIR$/app/src/main/java/com/android/calculator2/BkavMemoryFunction.java">[m
[31m-          <provider selected="true" editor-type-id="text-editor">[m
[31m-            <state relative-caret-position="302">[m
[31m-              <caret line="34" column="40" selection-start-line="34" selection-start-column="40" selection-end-line="34" selection-end-column="40" />[m
[31m-              <folding>[m
[31m-                <element signature="e#291#292#0" expanded="true" />[m
[31m-                <element signature="e#313#314#0" expanded="true" />[m
[31m-                <element signature="e#1373#1374#0" expanded="true" />[m
[31m-                <element signature="e#1402#1403#0" expanded="true" />[m
[31m-              </folding>[m
[31m-            </state>[m
[31m-          </provider>[m
[31m-        </entry>[m
[31m-      </file>[m
[31m-      <file pinned="false" current-in-tab="true">[m
[31m-        <entry file="file://$PROJECT_DIR$/app/build.gradle">[m
[31m-          <provider selected="true" editor-type-id="text-editor">[m
[31m-            <state relative-caret-position="276">[m
[31m-              <caret line="12" column="5" lean-forward="true" selection-start-line="12" selection-start-column="5" selection-end-line="12" selection-end-column="5" />[m
[31m-            </state>[m
[31m-          </provider>[m
[31m-        </entry>[m
[31m-      </file>[m
[31m-      <file pinned="false" current-in-tab="false">[m
[31m-        <entry file="file://$PROJECT_DIR$/build.gradle">[m
[31m-          <provider selected="true" editor-type-id="text-editor">[m
[31m-            <state relative-caret-position="414">[m
[31m-              <caret line="18" column="15" selection-start-line="18" selection-start-column="15" selection-end-line="18" selection-end-column="15" />[m
[31m-            </state>[m
[31m-          </provider>[m
[31m-        </entry>[m
[31m-      </file>[m
[31m-      <file pinned="false" current-in-tab="false">[m
[31m-        <entry file="file://$PROJECT_DIR$/app/src/main/java/com/android/calculator2/Evaluator.java">[m
[31m-          <provider selected="true" editor-type-id="text-editor">[m
[31m-            <state relative-caret-position="192">[m
[31m-              <caret line="582" column="51" selection-start-line="582" selection-start-column="51" selection-end-line="582" selection-end-column="51" />[m
[31m-              <folding>[m
[31m-                <element signature="imports" expanded="true" />[m
[31m-                <element signature="e#18623#18624#0" expanded="true" />[m
[31m-                <element signature="e#18667#18668#0" expanded="true" />[m
[31m-                <element signature="e#80190#80191#0" expanded="true" />[m
[31m-                <element signature="e#80244#80245#0" expanded="true" />[m
[31m-                <element signature="e#81261#81262#0" expanded="true" />[m
[31m-                <element signature="e#81297#81298#0" expanded="true" />[m
[31m-              </folding>[m
[31m-            </state>[m
[31m-          </provider>[m
[31m-        </entry>[m
[31m-      </file>[m
[31m-      <file pinned="false" current-in-tab="false">[m
[31m-        <entry file="file://$PROJECT_DIR$/app/src/main/java/com/android/calculator2/UnifiedReal.java">[m
[31m-          <provider selected="true" editor-type-id="text-editor">[m
[31m-            <state relative-caret-position="284">[m
[31m-              <caret line="631" lean-forward="true" selection-start-line="631" selection-end-line="631" />[m
[31m-              <folding>[m
[31m-                <element signature="e#23295#23296#0" expanded="true" />[m
[31m-                <element signature="e#23333#23334#0" expanded="true" />[m
[31m-              </folding>[m
[31m-            </state>[m
[31m-          </provider>[m
[31m-        </entry>[m
[31m-      </file>[m
[31m-      <file pinned="false" current-in-tab="false">[m
[31m-        <entry file="file://$PROJECT_DIR$/app/src/main/java/com/android/calculator2/CalculatorPadViewPager.java">[m
[31m-          <provider selected="true" editor-type-id="text-editor">[m
[31m-            <state relative-caret-position="118">[m
[31m-              <caret line="57" column="33" selection-start-line="57" selection-start-column="21" selection-end-line="57" selection-end-column="33" />[m
[31m-              <folding>[m
[31m-                <element signature="e#4572#4984#0" expanded="true" />[m
[31m-                <element signature="e#4983#4984#0" expanded="true" />[m
[31m-                <element signature="e#5882#5883#0" expanded="true" />[m
[31m-                <element signature="e#5929#5930#0" expanded="true" />[m
[31m-              </folding>[m
[31m-            </state>[m
[31m-          </provider>[m
[31m-        </entry>[m
[31m-      </file>[m
[31m-    </leaf>[m
[31m-  </component>[m
   <component name="FileTemplateManagerImpl">[m
     <option name="RECENT_TEMPLATES">[m
       <list>[m
         <option value="valueResourceFile" />[m
         <option value="Class" />[m
[32m+[m[32m        <option value="layoutResourceFile_vertical" />[m
       </list>[m
     </option>[m
   </component>[m
[31m-  <component name="FindInProjectRecents">[m
[31m-    <findStrings>[m
[31m-      <find>Flag</find>[m
[31m-      <find>Fl</find>[m
[31m-      <find>make</find>[m
[31m-      <find>m</find>[m
[31m-      <find>bar</find>[m
[31m-      <find>b</find>[m
[31m-      <find>C</find>[m
[31m-      <find>R.id.mo</find>[m
[31m-      <find>arraylist</find>[m
[31m-      <find>tiennv</find>[m
[31m-      <find>add(</find>[m
[31m-      <find>settext</find>[m
[31m-      <find>Operator</find>[m
[31m-      <find>log</find>[m
[31m-      <find>mOnContextMenuClickListener</find>[m
[31m-      <find>explicit</find>[m
[31m-      <find>mDecimalPt =</find>[m
[31m-      <find>mUnprocessedChars = null</find>[m
[31m-      <find>adapter</find>[m
[31m-      <find>mStaticPagerAdapter</find>[m
[31m-      <find>pad_operator</find>[m
[31m-      <find>setTranslationX</find>[m
[31m-      <find>anim</find>[m
[31m-      <find>anima</find>[m
[31m-      <find>mCurrentAnimator</find>[m
[31m-      <find>PageTransformer</find>[m
[31m-      <find>fling</find>[m
[31m-      <find>mEvaluator =</find>[m
[31m-      <find>Log</find>[m
[31m-      <find>Log</find>[m
[31m-    </findStrings>[m
[31m-    <replaceStrings>[m
[31m-      <replace />[m
[31m-    </replaceStrings>[m
[31m-  </component>[m
   <component name="Git.Settings">[m
     <option name="RECENT_GIT_ROOT_PATH" value="$PROJECT_DIR$" />[m
   </component>[m
[31m-  <component name="IdeDocumentHistory">[m
[31m-    <option name="CHANGED_PATHS">[m
[31m-      <list>[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/DragLayout.java" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/layout/fragment_history.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/layout/line_history.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/values-w520dp-h275dp-land/styles.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/layout/pad_operator_two_col.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/layout/pad_numeric.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/layout/activity_calculator_land.xml" />[m
[31m-        <option value="$USER_HOME$/Android/Sdk/platforms/android-29/data/res/values/strings.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/layout/pad_operator_one_col.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/layout/activity_calculator_tablet_port.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/values-w375dp-h500dp-port/layout.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/values-w230dp-h475dp-port/layout.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/values/layout.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/values-w230dp-h275dp/layout.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/values-w230dp-h375dp/layout.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/values-w375dp-h220dp/layout.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/values-w375dp-h375dp/layout.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/values-w375dp-h768dp-port/layout.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/values-w520dp-h768dp-port/layout.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/values-w375dp-h220dp/styles.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/values-w230dp-h375dp/styles.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/values-w230dp-h275dp/styles.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/values-w230dp-h475dp-port/styles.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/values/styles.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/values-w375dp-h768dp-port/dimens.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/CalculatorScrollView.java" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/layout/display_one_line.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/CalculatorDisplay.java" />[m
[31m-        <option value="$USER_HOME$/Android/Sdk/sources/android-29/android/widget/TextView.java" />[m
[31m-        <option value="$USER_HOME$/Android/Sdk/sources/android-29/android/widget/Editor.java" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/CalculatorFormula.java" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/CR.java" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/layout/display_two_line.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/layout-land/display_two_line.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/layout/toolbar.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/AndroidManifest.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/CalculatorResult.java" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/CalculatorExpr.java" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/KeyMaps.java" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/BkavAdvancedLayout.java" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/layout/pad_advanced_3x5.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/values-sw360dp-port/styles.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/res/layout/activity_calculator_port.xml" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/CalculatorPadViewPager.java" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/BkavMemoryFunction.java" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/Calculator.java" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/Evaluator.java" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/HistoryFragment.java" />[m
[31m-        <option value="$PROJECT_DIR$/app/src/main/java/com/android/calculator2/UnifiedReal.java" />[m
[31m-        <option value="$PROJECT_DIR$/app/build.gradle" />[m
[31m-        <option value="$PROJECT_DIR$/build.gradle" />[m
[31m-      </list>[m
[31m-    </option>[m
[31m-  </component>[m
[31m-  <component name="ProjectFrameBounds" extendedState="6">[m
[31m-    <option name="x" value="67" />[m
[31m-    <option name="y" value="25" />[m
[31m-    <option name="width" value="927" />[m
[31m-    <option name="height" value="1055" />[m
[31m-  </component>[m
[32m+[m[32m  <component name="ProjectId" id="1YNMUg2B0O490Q6yDaXBZFt7dvJ" />[m
   <component name="ProjectLevelVcsManager" settingsEditedManually="true" />[m
[31m-  <component name="ProjectView">[m
[31m-    <navigator proportions="" version="1">[m
[31m-      <foldersAlwaysOnTop value="true" />[m
[31m-    </navigator>[m
[31m-    <panes>[m
[31m-      <pane id="Scope">[m
[31m-        <subPane subId="Scope 'Default Changelist'; set:Default Changelist; class com.intellij.packageDependencies.ChangeListScope">[m
[31m-          <expand>[m
[31m-            <path>[m
[31m-              <item name="ExactCalculator" type="3d21c010:ScopeViewTreeModel$ProjectNode" />[m
[31m-              <item name="~/Desktop/ExactCalculator" type="442cc68d:ScopeViewTreeModel$RootNode" />[m
[31m-            </path>[m
[31m-            <path>[m
[31m-              <item name="ExactCalculator" type="3d21c010:ScopeViewTreeModel$ProjectNode" />[m
[31m-              <item name="~/Desktop/ExactCalculator" type="442cc68d:ScopeViewTreeModel$RootNode" />[m
[31m-              <item name="app" type="9f88c78c:ScopeViewTreeModel$FileNode" />[m
[31m-            </path>[m
[31m-            <path>[m
[31m-              <item name="ExactCalculator" type="3d21c010:ScopeViewTreeModel$ProjectNode" />[m
[31m-              <item name="~/Desktop/ExactCalculator" type="442cc68d:ScopeViewTreeModel$RootNode" />[m
[31m-              <item name="app" type="9f88c78c:ScopeViewTreeModel$FileNode" />[m
[31m-              <item name="src" type="9f88c78c:ScopeViewTreeModel$FileNode" />[m
[31m-            </path>[m
[31m-            <path>[m
[31m-              <item name="ExactCalculator" type="3d21c010:ScopeViewTreeModel$ProjectNode" />[m
[31m-              <item name="~/Desktop/ExactCalculator" type="442cc68d:ScopeViewTreeModel$RootNode" />[m
[31m-              <item name="app" type="9f88c78c:ScopeViewTreeModel$FileNode" />[m
[31m-              <item name="src" type="9f88c78c:ScopeViewTreeModel$FileNode" />[m
[31m-              <item name="main" type="9f88c78c:ScopeViewTreeModel$FileNode" />[m
[31m-            </path>[m
[31m-            <path>[m
[31m-              <item name="ExactCalculator" type="3d21c010:ScopeViewTreeModel$ProjectNode" />[m
[31m-              <item name="~/Desktop/ExactCalculator" type="442cc68d:ScopeViewTreeModel$RootNode" />[m
[31m-              <item name="app" type="9f88c78c:ScopeViewTreeModel$FileNode" />[m
[31m-              <item name="src" type="9f88c78c:ScopeViewTreeModel$FileNode" />[m
[31m-              <item name="main" type="9f88c78c:ScopeViewTreeModel$FileNode" />[m
[31m-              <item name="res" type="9f88c78c:ScopeViewTreeModel$FileNode" />[m
[31m-            </path>[m
[31m-            <path>[m
[31m-              <item name="ExactCalculator" type="3d21c010:ScopeViewTreeModel$ProjectNode" />[m
[31m-              <item name="~/Desktop/ExactCalculator" type="442cc68d:ScopeViewTreeModel$RootNode" />[m
[31m-              <item name="app" type="9f88c78c:ScopeViewTreeModel$FileNode" />[m
[31m-              <item name="src" type="9f88c78c:ScopeViewTreeModel$FileNode" />[m
[31m-              <item name="main" type="9f88c78c:ScopeViewTreeModel$FileNode" />[m
[31m-              <item name="res" type="9f88c78c:ScopeViewTreeModel$FileNode" />[m
[31m-              <item name="layout" type="9f88c78c:ScopeViewTreeModel$FileNode" />[m
[31m-            </path>[m
[31m-          </expand>[m
[31m-          <select />[m
[31m-        </subPane>[m
[31m-      </pane>[m
[31m-      <pane id="AndroidView">[m
[31m-        <subPane>[m
[31m-          <expand>[m
[31m-            <path>[m
[31m-              <item name="BkavCalculator" type="1abcf292:AndroidViewProjectNode" />[m
[31m-              <item name="app" type="feadf853:AndroidModuleNode" />[m
[31m-            </path>[m
[31m-            <path>[m
[31m-              <item name="BkavCalculator" type="1abcf292:AndroidViewProjectNode" />[m
[31m-              <item name="app" type="feadf853:AndroidModuleNode" />[m
[31m-              <item name="java" type="edd41e36:AndroidSourceTypeNode" />[m
[31m-            </path>[m
[31m-            <path>[m
[31m-              <item name="BkavCalculator" type="1abcf292:AndroidViewProjectNode" />[m
[31m-              <item name="app" type="feadf853:AndroidModuleNode" />[m
[31m-              <item name="java" type="edd41e36:AndroidSourceTypeNode" />[m
[31m-              <item name="calculator2" type="cbb59c9e:AndroidPsiDirectoryNode" />[m
[31m-            </path>[m
[31m-            <path>[m
[31m-              <item name="BkavCalculator" type="1abcf292:AndroidViewProjectNode" />[m
[31m-              <item name="Gradle Scripts" type="ae0cef3a:AndroidBuildScriptsGroupNode" />[m
[31m-            </path>[m
[31m-          </expand>[m
[31m-          <select />[m
[31m-        </subPane>[m
[31m-      </pane>[m
[31m-      <pane id="ProjectPane">[m
[31m-        <subPane>[m
[31m-          <expand>[m
[31m-            <path>[m
[31m-              <item name="BkavCalculator" type="b2602c69:ProjectViewProjectNode" />[m
[31m-              <item name="BkavCalculator" type="8a07ba80:GradleTreeStructureProvider$GradleModuleDirectoryNode" />[m
[31m-            </path>[m
[31m-          </expand>[m
[31m-          <select />[m
[31m-        </subPane>[m
[31m-      </pane>[m
[31m-      <pane id="PackagesPane" />[m
[31m-    </panes>[m
[31m-  </component>[m
   <component name="PropertiesComponent">[m
     <property name="DEBUGGABLE_DEVICE" value="bkav-bphone_b1114-1234567" />[m
     <property name="DEBUGGABLE_PROCESS" value="com.android.calculator2" />[m
[36m@@ -446,7 +92,7 @@[m
     <property name="android.sdk.path" value="/media/quangndb/SubSystem/sdk" />[m
     <property name="external.system.task.project.file.to.start" value="$PROJECT_DIR$/app" />[m
     <property name="last_directory_selection" value="$PROJECT_DIR$/app/src/main/res/mipmap-xxhdpi" />[m
[31m-    <property name="last_opened_file_path" value="$PROJECT_DIR$" />[m
[32m+[m[32m    <property name="last_opened_file_path" value="$PROJECT_DIR$/../appBkav/MasterCalculate/ExactCalculator" />[m
     <property name="project.structure.last.edited" value="Modules" />[m
     <property name="project.structure.proportion" value="0.17" />[m
     <property name="project.structure.side.proportion" value="0.2" />[m
[36m@@ -530,8 +176,6 @@[m
       <option name="SKIP_NOOP_APK_INSTALLATIONS" value="true" />[m
       <option name="FORCE_STOP_RUNNING_APP" value="true" />[m
       <option name="TARGET_SELECTION_MODE" value="DEVICE_AND_SNAPSHOT_COMBO_BOX" />[m
[31m-      <option name="USE_LAST_SELECTED_DEVICE" value="false" />[m
[31m-      <option name="PREFERRED_AVD" value="" />[m
       <option name="SELECTED_CLOUD_MATRIX_CONFIGURATION_ID" value="-1" />[m
       <option name="SELECTED_CLOUD_MATRIX_PROJECT_ID" value="" />[m
       <option name="DEBUGGER_TYPE" value="Auto" />[m
[36m@@ -653,69 +297,18 @@[m
       <is-autoscroll-to-source value="true" />[m
     </todo-panel>[m
   </component>[m
[31m-  <component name="ToolWindowManager">[m
[31m-    <frame x="67" y="25" width="1853" height="1055" extended-state="6" />[m
[31m-    <layout>[m
[31m-      <window_info id="Captures" order="0" side_tool="true" weight="0.25" />[m
[31m-      <window_info active="true" content_ui="combo" id="Project" order="1" sideWeight="0.49676377" visible="true" weight="0.16436082" />[m
[31m-      <window_info id="Structure" order="2" sideWeight="0.50359714" side_tool="true" weight="0.21195352" />[m
[31m-      <window_info id="Favorites" order="3" sideWeight="0.50323623" side_tool="true" weight="0.21195352" />[m
[31m-      <window_info id="Build Variants" order="4" side_tool="true" />[m
[31m-      <window_info id="Image Layers" order="5" />[m
[31m-      <window_info id="Designer" order="6" />[m
[31m-      <window_info id="Resources Explorer" order="7" weight="0.14056447" />[m
[31m-      <window_info id="Capture Tool" order="8" />[m
[31m-      <window_info anchor="bottom" id="Run" order="0" weight="0.31965443" />[m
[31m-      <window_info anchor="bottom" x="90" y="358" width="1807" height="388" id="Logcat" order="1" sideWeight="0.4975097" weight="0.32829374" />[m
[31m-      <window_info anchor="bottom" id="TODO" order="2" sideWeight="0.4997233" weight="0.32937366" />[m
[31m-      <window_info anchor="bottom" id="Find" order="3" weight="0.33693305" />[m
[31m-      <window_info anchor="bottom" id="Terminal" order="4" sideWeight="0.4997233" weight="0.1673866" />[m
[31m-      <window_info anchor="bottom" id="Event Log" order="5" sideWeight="0.50249034" side_tool="true" weight="0.23434125" />[m
[31m-      <window_info anchor="bottom" id="Version Control" order="6" weight="0.32721382" />[m
[31m-      <window_info anchor="bottom" id="Build" order="7" sideWeight="0.4986165" visible="true" weight="0.15766738" />[m
[31m-      <window_info anchor="bottom" id="Debug" order="8" weight="0.36825055" />[m
[31m-      <window_info anchor="bottom" id="Android Profiler" order="9" />[m
[31m-      <window_info anchor="right" id="Capture Analysis" order="0" />[m
[31m-      <window_info anchor="right" id="Gradle" order="1" weight="0.32982844" />[m
[31m-      <window_info anchor="right" id="Theme Preview" order="2" />[m
[31m-      <window_info anchor="right" id="Key Promoter X" order="3" />[m
[31m-      <window_info anchor="right" id="Palette&#9;" order="4" />[m
[31m-      <window_info anchor="right" id="Preview" order="5" sideWeight="0.05399568" weight="0.32374102" />[m
[31m-      <window_info anchor="right" id="Device File Explorer" order="6" sideWeight="0.94600433" side_tool="true" weight="0.3370227" />[m
[31m-      <window_info anchor="right" id="Assistant" order="7" weight="0.32982844" />[m
[31m-      <window_info anchor="right" id="Hierarchy" order="8" weight="0.32982844" />[m
[31m-      <window_info anchor="right" x="0" y="0" width="413" height="855" id="Documentation" order="9" side_tool="true" weight="0.32982844" />[m
[31m-    </layout>[m
[31m-    <layout-to-restore>[m
[31m-      <window_info id="Captures" order="0" side_tool="true" weight="0.25" />[m
[31m-      <window_info content_ui="combo" id="Project" order="1" sideWeight="0.49676377" visible="true" weight="0.16436082" />[m
[31m-      <window_info id="Structure" order="2" sideWeight="0.50359714" side_tool="true" weight="0.21195352" />[m
[31m-      <window_info id="Favorites" order="3" sideWeight="0.50323623" side_tool="true" weight="0.21195352" />[m
[31m-      <window_info id="Build Variants" order="4" side_tool="true" />[m
[31m-      <window_info id="Image Layers" order="5" />[m
[31m-      <window_info id="Designer" order="6" />[m
[31m-      <window_info id="Resources Explorer" order="7" weight="0.14056447" />[m
[31m-      <window_info id="Capture Tool" order="8" />[m
[31m-      <window_info anchor="bottom" id="Run" order="0" weight="0.2062635" />[m
[31m-      <window_info anchor="bottom" x="90" y="358" width="1807" height="388" id="Logcat" order="1" sideWeight="0.4975097" weight="0.35961124" />[m
[31m-      <window_info anchor="bottom" id="TODO" order="2" sideWeight="0.4997233" weight="0.32937366" />[m
[31m-      <window_info anchor="bottom" id="Find" order="3" weight="0.33693305" />[m
[31m-      <window_info anchor="bottom" id="Terminal" order="4" sideWeight="0.4997233" weight="0.1673866" />[m
[31m-      <window_info anchor="bottom" id="Event Log" order="5" sideWeight="0.50249034" side_tool="true" weight="0.23434125" />[m
[31m-      <window_info anchor="bottom" id="Version Control" order="6" weight="0.32721382" />[m
[31m-      <window_info anchor="bottom" id="Build" order="7" sideWeight="0.4986165" weight="0.032397408" />[m
[31m-      <window_info active="true" anchor="bottom" id="Debug" order="8" visible="true" weight="0.26673865" />[m
[31m-      <window_info anchor="bottom" id="Android Profiler" order="9" />[m
[31m-      <window_info anchor="right" id="Capture Analysis" order="0" />[m
[31m-      <window_info anchor="right" id="Gradle" order="1" weight="0.32982844" />[m
[31m-      <window_info anchor="right" id="Theme Preview" order="2" />[m
[31m-      <window_info anchor="right" id="Key Promoter X" order="3" />[m
[31m-      <window_info anchor="right" id="Palette&#9;" order="4" />[m
[31m-      <window_info anchor="right" id="Preview" order="5" sideWeight="0.05399568" weight="0.32374102" />[m
[31m-      <window_info anchor="right" id="Device File Explorer" order="6" sideWeight="0.94600433" side_tool="true" weight="0.3370227" />[m
[31m-      <window_info anchor="right" id="Assistant" order="7" weight="0.32982844" />[m
[31m-      <window_info anchor="right" id="Hierarchy" order="8" weight="0.32982844" />[m
[31m-    </layout-to-restore>[m
[32m+[m[32m  <component name="Vcs.Log.Tabs.Properties">[m
[32m+[m[32m    <option name="TAB_STATES">[m
[32m+[m[32m      <map>[m
[32m+[m[32m        <entry key="MAIN">[m
[32m+[m[32m          <value>[m
[32m+[m[32m            <State>[m
[32m+[m[32m              <option name="COLUMN_ORDER" />[m
[32m+[m[32m            </State>[m
[32m+[m[32m          </value>[m
[32m+[m[32m        </entry>[m
[32m+[m[32m      </map>[m
[32m+[m[32m    </option>[m
   </component>[m
   <component name="VcsManagerConfiguration">[m
     <MESSAGE value="C7/11" />[m
[36m@@ -738,462 +331,6 @@[m
       </expression>[m
     </expressions>[m
   </component>[m
[31m-  <component name="editorHistoryManager">[m
[31m-    <entry file="file://$USER_HOME$/Android/Sdk/sources/android-29/java/text/DecimalFormatSymbols.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="185">[m
[31m-          <caret line="203" column="16" selection-start-line="203" selection-start-column="16" selection-end-line="203" selection-end-column="16" />[m
[31m-          <folding>[m
[31m-            <element signature="e#8088#8089#0" expanded="true" />[m
[31m-            <element signature="e#8127#8128#0" expanded="true" />[m
[31m-          </folding>[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$USER_HOME$/Android/Sdk/sources/android-29/java/lang/Class.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="11983">[m
[31m-          <caret line="577" selection-start-line="577" selection-end-line="577" />[m
[31m-          <folding>[m
[31m-            <element signature="e#24933#24934#0" expanded="true" />[m
[31m-            <element signature="e#24995#24996#0" expanded="true" />[m
[31m-            <element signature="e#25259#25260#0" expanded="true" />[m
[31m-            <element signature="e#25308#25309#0" expanded="true" />[m
[31m-          </folding>[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$USER_HOME$/Android/Sdk/sources/android-29/android/os/Handler.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="226">[m
[31m-          <caret line="873" selection-start-line="873" selection-end-line="873" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$USER_HOME$/Android/Sdk/sources/android-29/android/os/MessageQueue.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="161">[m
[31m-          <caret line="34" column="69" lean-forward="true" selection-start-line="34" selection-start-column="69" selection-end-line="34" selection-end-column="69" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$USER_HOME$/Android/Sdk/sources/android-29/java/lang/Character.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="109526">[m
[31m-          <caret line="4807" column="18" selection-start-line="4789" selection-start-column="7" selection-end-line="4807" selection-end-column="18" />[m
[31m-          <folding>[m
[31m-            <element signature="e#155807#155808#0" expanded="true" />[m
[31m-            <element signature="e#155835#155836#0" expanded="true" />[m
[31m-            <element signature="e#244877#244878#0" expanded="true" />[m
[31m-            <element signature="e#244921#244922#0" expanded="true" />[m
[31m-            <element signature="e#255097#255098#0" expanded="true" />[m
[31m-            <element signature="e#255140#255141#0" expanded="true" />[m
[31m-          </folding>[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$USER_HOME$/Android/Sdk/sources/android-29/android/app/Activity.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="57569">[m
[31m-          <caret line="2687" selection-start-line="2687" selection-end-line="2687" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$USER_HOME$/Android/Sdk/sources/android-29/android/view/Window.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="29233">[m
[31m-          <caret line="1348" selection-start-line="1348" selection-end-line="1348" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$USER_HOME$/Android/Sdk/sources/android-29/com/android/internal/policy/PhoneWindow.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="44252">[m
[31m-          <caret line="2072" selection-start-line="2072" selection-end-line="2072" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$USER_HOME$/Android/Sdk/sources/android-29/java/lang/Math.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="28405">[m
[31m-          <caret line="1273" selection-start-line="1273" selection-end-line="1273" />[m
[31m-          <folding>[m
[31m-            <element signature="e#50967#50968#0" expanded="true" />[m
[31m-          </folding>[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/app/src/main/java/com/android/calculator2/KeyMaps.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="45">[m
[31m-          <caret line="530" column="23" selection-start-line="530" selection-start-column="23" selection-end-line="530" selection-end-column="23" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/captures/com.android.calculator2_2020.02.14_15.39.li">[m
[31m-      <provider selected="true" editor-type-id="capture-editor" />[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/captures/com.android.calculator2_2020.02.14_15.42.li">[m
[31m-      <provider selected="true" editor-type-id="capture-editor" />[m
[31m-    </entry>[m
[31m-    <entry file="file://$USER_HOME$/Android/Sdk/sources/android-29/android/view/Choreographer.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="183">[m
[31m-          <caret line="816" selection-start-line="816" selection-end-line="816" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/captures/com.android.calculator2_2020.02.14_16.23.li">[m
[31m-      <provider selected="true" editor-type-id="capture-editor" />[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/app/src/main/java/com/android/calculator2/BkavHistoryLayout.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="-46">[m
[31m-          <caret line="10" column="13" selection-start-line="10" selection-start-column="13" selection-end-line="10" selection-end-column="13" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/captures/com.android.calculator2_2020.02.14_17.43.li">[m
[31m-      <provider selected="true" editor-type-id="capture-editor" />[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/app/src/main/res/layout/pad_advanced_3x5.xml">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="506">[m
[31m-          <caret line="22" column="45" selection-start-line="22" selection-start-column="45" selection-end-line="22" selection-end-column="45" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-      <provider editor-type-id="android-designer2" />[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/captures/com.android.calculator2_2020.02.14_17.45.li">[m
[31m-      <provider selected="true" editor-type-id="capture-editor" />[m
[31m-    </entry>[m
[31m-    <entry file="file://$USER_HOME$/Android/Sdk/sources/android-29/android/widget/EditText.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="1955">[m
[31m-          <caret line="113" selection-start-line="113" selection-end-line="113" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$USER_HOME$/Android/Sdk/sources/android-29/android/widget/TextView.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="45126">[m
[31m-          <caret line="2179" selection-start-line="2179" selection-end-line="2179" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/app/src/main/java/com/android/calculator2/BkavAdvancedLayout.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="874">[m
[31m-          <caret line="50" column="27" selection-start-line="50" selection-start-column="27" selection-end-line="50" selection-end-column="27" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/app/src/main/java/com/android/calculator2/DragLayout.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="137">[m
[31m-          <caret line="75" selection-start-line="75" selection-end-line="75" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/captures/com.android.calculator2_2020.02.15_08.50.li">[m
[31m-      <provider selected="true" editor-type-id="capture-editor" />[m
[31m-    </entry>[m
[31m-    <entry file="jar://$USER_HOME$/.gradle/caches/modules-2/files-2.1/androidx.viewpager/viewpager/1.0.0/db045f92188b9d247d5f556866f8861ab68528f0/viewpager-1.0.0-sources.jar!/androidx/viewpager/widget/PagerAdapter.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="146">[m
[31m-          <caret line="80" column="22" selection-start-line="80" selection-start-column="22" selection-end-line="80" selection-end-column="22" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/app/src/main/res/values/attr.xml">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="-266" />[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/app/src/main/res/values-w230dp-h275dp/styles.xml">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="195">[m
[31m-          <caret line="38" column="17" selection-start-line="38" selection-start-column="17" selection-end-line="38" selection-end-column="17" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/app/src/main/res/layout/activity_calculator_land.xml">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="52">[m
[31m-          <caret line="39" column="21" lean-forward="true" selection-start-line="39" selection-start-column="21" selection-end-line="39" selection-end-column="21" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-      <provider editor-type-id="android-designer2" />[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/app/src/main/res/values/styles.xml">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="233">[m
[31m-          <caret line="133" column="51" lean-forward="true" selection-start-line="133" selection-start-column="51" selection-end-line="133" selection-end-column="51" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/app/src/main/res/values-sw360dp-port/styles.xml">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="241">[m
[31m-          <caret line="69" column="41" selection-start-line="69" selection-start-column="28" selection-end-line="69" selection-end-column="41" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/app/src/main/res/layout/pad_numeric.xml">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="276">[m
[31m-          <caret line="21" column="28" lean-forward="true" selection-start-line="21" selection-start-column="28" selection-end-line="21" selection-end-column="28" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-      <provider editor-type-id="android-designer2" />[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/captures/com.android.calculator2_2020.02.15_09.09.li">[m
[31m-      <provider selected="true" editor-type-id="capture-editor" />[m
[31m-    </entry>[m
[31m-    <entry file="jar://$USER_HOME$/.gradle/caches/modules-2/files-2.1/androidx.viewpager/viewpager/1.0.0/db045f92188b9d247d5f556866f8861ab68528f0/viewpager-1.0.0-sources.jar!/androidx/viewpager/widget/ViewPager.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="198">[m
[31m-          <caret line="766" column="16" selection-start-line="766" selection-start-column="16" selection-end-line="766" selection-end-column="16" />[m
[31m-          <folding>[m
[31m-            <element signature="e#21896#21897#0" expanded="true" />[m
[31m-            <element signature="e#21927#21928#0" expanded="true" />[m
[31m-            <element signature="e#41427#41428#0" expanded="true" />[m
[31m-            <element signature="e#41461#41462#0" expanded="true" />[m
[31m-          </folding>[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$USER_HOME$/Android/Sdk/sources/android-29/android/view/View.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="190">[m
[31m-          <caret line="15523" column="21" selection-start-line="15523" selection-start-column="21" selection-end-line="15523" selection-end-column="21" />[m
[31m-          <folding>[m
[31m-            <element signature="e#638946#638947#0" expanded="true" />[m
[31m-            <element signature="e#638983#638984#0" expanded="true" />[m
[31m-          </folding>[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/captures/com.android.calculator2_2020.02.15_09.33.li">[m
[31m-      <provider selected="true" editor-type-id="capture-editor" />[m
[31m-    </entry>[m
[31m-    <entry file="file://$USER_HOME$/Android/Sdk/sources/android-29/android/view/GestureDetector.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="195">[m
[31m-          <caret line="384" column="11" selection-start-line="384" selection-start-column="11" selection-end-line="384" selection-end-column="11" />[m
[31m-          <folding>[m
[31m-            <element signature="e#8195#8196#0" expanded="true" />[m
[31m-            <element signature="e#8231#8232#0" expanded="true" />[m
[31m-          </folding>[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/app/src/main/res/layout/activity_calculator_main.xml">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="336">[m
[31m-          <caret line="25" column="30" lean-forward="true" selection-start-line="25" selection-start-column="30" selection-end-line="25" selection-end-column="30" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-      <provider editor-type-id="android-designer2" />[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/app/src/main/res/layout/pad_operator_one_col.xml">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="-975">[m
[31m-          <caret line="12" column="69" lean-forward="true" selection-start-line="12" selection-start-column="69" selection-end-line="12" selection-end-column="69" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-      <provider editor-type-id="android-designer2" />[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/app/src/main/res/layout/activity_calculator_port.xml">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="1449">[m
[31m-          <caret line="63" lean-forward="true" selection-start-line="63" selection-end-line="63" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-      <provider editor-type-id="android-designer2" />[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/app/src/main/res/layout/display_one_line.xml">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="-1205" />[m
[31m-      </provider>[m
[31m-      <provider editor-type-id="android-designer2" />[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/app/src/main/res/values/layout.xml">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="340">[m
[31m-          <caret line="19" column="50" lean-forward="true" selection-start-line="19" selection-start-column="50" selection-end-line="19" selection-end-column="50" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/app/src/main/java/com/android/calculator2/BkavMemoryFunction.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="302">[m
[31m-          <caret line="34" column="40" selection-start-line="34" selection-start-column="40" selection-end-line="34" selection-end-column="40" />[m
[31m-          <folding>[m
[31m-            <element signature="e#291#292#0" expanded="true" />[m
[31m-            <element signature="e#313#314#0" expanded="true" />[m
[31m-            <element signature="e#1373#1374#0" expanded="true" />[m
[31m-            <element signature="e#1402#1403#0" expanded="true" />[m
[31m-          </folding>[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$USER_HOME$/Android/Sdk/sources/android-29/android/util/Log.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="557">[m
[31m-          <caret line="144" selection-start-line="144" selection-end-line="144" />[m
[31m-        </state>[m
[31m-      </provider>[m
[31m-    </entry>[m
[31m-    <entry file="file://$PROJECT_DIR$/app/src/main/java/com/android/calculator2/Calculator.java">[m
[31m-      <provider selected="true" editor-type-id="text-editor">[m
[31m-        <state relative-caret-position="103">[m
[31m-          <caret line="197" column="9" selection-start-line="197" selection-start-column="9" selection-end-line="197" selection-end-column="9" />[m
[31m-          <folding>[m
[31m-            <element signature="e#0#115599#0" expanded="true" />[m
[31m-            <element signature="imports" expanded="true" />[m
[31m-            <element signature="e#6841#6860#0" expanded="true" />[m
[31m-            <element signature="e#6971#6972#0" expanded="true" />[m
[31m-            <element signature="e#7048#7049#0" expanded="true" />[m
[31m-            <element signature="e#7942#8424#0" expanded="true" />[m
[31m-            <element signature="e#8423#8424#0" expanded="true" />[m
[31m-            <element signature="e#8573#8574#0" expanded="true" />[m
[31m-            <element signature="e#8632#8633#0" expanded="true" />[m
[31m-            <element signature="e#9075#9290#0" expanded="true" />[m
[31m-            <element signature="e#9289#9290#0" expanded="true" />[m
[31m-            <element signature="e#18052#18273#0" expanded="true" />[m
[31m-            <element signature="e#18272#18273#0" expanded="true" />[m
[31m-            <element signature="e#22014#22244#0" expanded="true" />[m
[31m-            <element signature="e#22243#22244#0" expanded="true" />[m
[31m-            <element signature="e#22352#25824#0" expanded="true" />[m
[31m-            <element signature="e#25823#25824#0" expanded="true" />[m
[31m-            <element signature="e#26661#28484#0" expanded="true" />[m
[31m-            <element signature="e#28483#28484#0" expanded="true" />[m
[31m-            <element signature="e#30621#31574#0" expanded="true" />[m
[31m-            <element signature="e#31573#31574#0" expanded="true" />[m
[31m-            <element signature="e#55616#55617#0" expanded="true" />[m
[31m-            <element signature="e#55649#55650#0" expanded="true" />[m
[31m-            <element signature="e#56282#56283#0" expanded="true" />[m
[31m-            <element signature="e#56327#56328#0" expanded="true" />[m
[31m-            <element signature="e#60561#60592#0" expanded="true" />[m
[31m-            <element signature="e#61347#61379#0" expanded="true" />[m
[31m-            <element signature="e#62434#62451#0" expanded="true" />[m
[31m-            <element signature="e#62498#62531#0" expanded="true" />[m
[31m-            <element signature="e#62567#62584#0" expanded="true" />[m
[31m-            <element signature="e#62633#62668#0" expanded="true" />[m
[31m-            <element signature="e#62718#62735#0" expanded="true" />[m
[31m-            <element signature="e#62782#62815#0" expanded="true" />[m
[31m-            <element signature="e#62851#62868#0" expanded="true" />[m
[31m-            <element signature="e#62917#62952#0" expanded="true" />[m
[31m-            <element signature="e#79011#79043#0" expanded="true" />[m
[31m-            <element signature="e#83975#83996#0" expanded="true" />[m
[31m-            <element signature="e#94490#94717#0" expanded="true" />[m
[31m-            <element signature="e#94716#94717#0" expanded="true" />[m
[31m-            <element signature="e#94895#94937#0" expanded="true" />[m
[31m-            <element signature="e#95447#95661#0" expanded="true" />[m
[31m-    