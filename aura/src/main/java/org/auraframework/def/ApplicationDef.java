/*
 * Copyright (C) 2013 salesforce.com, inc.
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
package org.auraframework.def;

import java.util.List;

import org.auraframework.throwable.quickfix.QuickFixException;

/**
 */
public interface ApplicationDef extends BaseComponentDef {
    @Override
    DefDescriptor<ApplicationDef> getDescriptor();

    @Override
    DefDescriptor<ApplicationDef> getExtendsDescriptor();

    DefDescriptor<LayoutsDef> getLayoutsDefDescriptor();

    DefDescriptor<EventDef> getLocationChangeEventDescriptor() throws QuickFixException;

    Boolean isAppcacheEnabled() throws QuickFixException;

    List<String> getAdditionalAppCacheURLs() throws QuickFixException;

    Boolean isOnePageApp() throws QuickFixException;

    /**
     * Gets the theme override, if present.
     * 
     * Note that this differs from the "local theme" ({@link #getLocalThemeDescriptor()}), as this is specifically the
     * application-wide override theme. In contrast, the local theme is applicable only to its component/app bundle.
     * However, for applications only, the local theme and override theme may refer to the same {@link ThemeDef} (if the
     * app override theme is in the app bundle).
     */
    DefDescriptor<ThemeDef> getOverrideThemeDescriptor();
}
