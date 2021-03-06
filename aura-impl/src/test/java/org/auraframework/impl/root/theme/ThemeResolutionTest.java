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
package org.auraframework.impl.root.theme;

import org.auraframework.def.DefDescriptor;
import org.auraframework.def.StyleDef;
import org.auraframework.def.ThemeDef;
import org.auraframework.impl.css.StyleTestCase;
import org.auraframework.throwable.AuraRuntimeException;
import org.auraframework.throwable.quickfix.QuickFixException;

/**
 * Unit tests for resolving theme function values in CSS files.
 */
public class ThemeResolutionTest extends StyleTestCase {
    public ThemeResolutionTest(String name) {
        super(name);
    }

    private void assertStyle(DefDescriptor<StyleDef> style, String expected) throws QuickFixException {
        expected = expected.replace(".THIS", "." + style.getDef().getClassName());
        assertEquals("Did not get the expected css code", expected, style.getDef().getCode());
    }

    /** where the variable value is unquoted */
    public void testUnquoted() throws Exception {
        addNsTheme(theme().var("color", "red"));
        String src = ".THIS {color: theme(color);}";
        assertStyle(addStyleDef(src), ".THIS {color:red}");
    }

    /** where the variable value is double quoted */
    public void testDoubleQuoted() throws Exception {
        addNsTheme(theme().var("color", "red"));
        String src = ".THIS {color: theme(\"color\");}";
        assertStyle(addStyleDef(src), ".THIS {color:red}");
    }

    /** proper stripping of outer quotes when using concatenation */
    public void testQuotedConcatenation() throws Exception {
        addNsTheme(theme()
                .var("margin", "10px")
                .var("font", "Times")
                .var("gradient", "red, yellow"));

        String src = ".THIS {\n" +
                "  margin: theme('5px ' + margin);" + // quotes should not be stripped
                "  font-family: theme(\"font + ',Arial'\");\n" + // quotes should be stripped
                "  margin: theme('5px ' + margin + ' 10px');\n" + // quotes should not be stripped
                "  background: t('linear-grad('+ gradient +')');\n" + // quotes should not be stripped
                "}";

        String expected = ".THIS {" +
                "margin:5px 10px; " +
                "font-family:Times,Arial; " +
                "margin:5px 10px 10px; " +
                "background:linear-grad(red, yellow)" +
                "}";

        assertStyle(addStyleDef(src), expected);
    }

    /** where the variable value is inherited */
    public void testInherited() throws Exception {
        DefDescriptor<ThemeDef> parent = addSeparateTheme(theme().var("color", "red"));
        addNsTheme(theme().parent(parent));

        String src = ".THIS {color: theme(color);}";
        assertStyle(addStyleDef(src), ".THIS {color:red}");
    }

    /** where the variable is imported */
    public void testImported() throws Exception {
        DefDescriptor<ThemeDef> imported = addSeparateTheme(theme().var("color", "red"));
        addNsTheme(theme().imported(imported));

        String src = ".THIS {color: theme(color);}";
        assertStyle(addStyleDef(src), ".THIS {color:red}");
    }

    /** where the variable value is overridden */
    public void testOverridden() throws Exception {
        DefDescriptor<ThemeDef> parent = addSeparateTheme(theme().var("color", "red"));
        addNsTheme(theme().parent(parent).var("color", "blue"));

        String src = ".THIS {color: theme(color);}";
        assertStyle(addStyleDef(src), ".THIS {color:blue}");
    }

    /** using the 't' alternative function name */
    public void testShorthand() throws Exception {
        addNsTheme(theme().var("color", "red"));
        String src = ".THIS {color: t(color);}";
        assertStyle(addStyleDef(src), ".THIS {color:red}");
    }

    /** using multiple themes functions in one declaration value */
    public void testMultipleThemeFunctions() throws Exception {
        addNsTheme(theme().var("marginTB", "7px").var("marginLR", "5px"));
        String src = ".THIS {margin:t(marginTB) t(marginLR)}";
        assertStyle(addStyleDef(src), ".THIS {margin:7px 5px}");
    }

    /** errors when the theme does not exist */
    public void testNonexistentTheme() throws Exception {
        try {
            addStyleDef(".THIS{color: theme(color)").getDef().getCode();
            fail("expected exception");
        } catch (Exception e) {}
    }

    /** errors when the variable does not exist */
    public void testNonexistentVariable() throws Exception {
        addNsTheme(theme().var("color", "red"));
        try {
            addStyleDef(".THIS{color: theme(dolor)").getDef().getCode();
            fail("expected exception");
        } catch (Exception e) {}
    }

    /** if the variable value is an empty string then the declaration should be removed */
    public void testDeclarationRemoval() throws Exception {
        addNsTheme(theme().var("color", ""));
        String src = ".THIS {color: theme(color); font: arial}";
        assertStyle(addStyleDef(src), ".THIS {font:arial}");
    }

    /** test expressions */
    public void testExpression() throws Exception {
        addNsTheme(theme()
                .var("margin", "10px")
                .var("spacious", "true")
                .var("lineHeight", "5"));

        String src = ".THIS {\n" +
                "  margin: t('5px ' + margin);\n" +
                "  margin: t(margin + ' 5px');\n" +
                "  margin: t(margin + ' 5px ' + margin);\n" +
                // "  padding: t(spacious == true ? '30px' : '5px');\n" +
                // "  line-height: t(lineHeight * 2);\n" +
                "}";

        String expected = ".THIS {" +
                "margin:5px 10px; " +
                "margin:10px 5px; " +
                "margin:10px 5px 10px" +
                // "padding:30px; " +
                // "line-height:10;" +
                "}";

        assertStyle(addStyleDef(src), expected);
    }

    /** theme in media query */
    public void testThemeInMediaQuery() throws Exception {
        addNsTheme(theme().var("normal", "only screen and (max-width: 999px) and (orientation: portrait)"));

        String src = "@media theme(normal) {\n" +
                "  .THIS {color:red}  \n" +
                "}";

        String expected = "@media only screen and (max-width: 999px) and (orientation: portrait) {\n" +
                "  .THIS {color:red}\n" +
                "}";

        assertStyle(addStyleDef(src), expected);
    }

    /** theme in media query has error */
    public void testThemeInMediaQueryHasError() throws Exception {
        addNsTheme(theme().var("normal", "screen (max-width: 999px)"));

        String src = "@media theme(normal) {\n" +
                "  .THIS {color:red}  \n" +
                "}";

        try {
            addStyleDef(src).getDef().getCode();
            fail("expected exception");
        } catch (Exception e) {
            checkExceptionContains(e, AuraRuntimeException.class, "Expected to find keyword");
        }
    }

    /** theme in media query cannot evaluate to an empty string */
    public void testThemeInMediaQueryEvalsToEmpty() throws Exception {
        addNsTheme(theme().var("normal", ""));

        String src = "@media theme(normal) {\n" +
                "  .THIS {color:red}  \n" +
                "}";

        try {
            addStyleDef(src).getDef().getCode();
            fail("expected exception");
        } catch (Exception e) {
            checkExceptionContains(e, AuraRuntimeException.class, "must not evaluate to an empty string");
        }
    }

    /** themes with auto-prefixable properties */
    public void testThemeAutoPrefix() throws Exception {
        addNsTheme(theme()
                .var("sizing", "border-box")
                .var("gradient", "red, yellow"));

        String src = ".THIS {\n" +
                "  box-sizing: t(sizing);\n" +
                "  background: theme('linear-gradient(' + gradient + ')');\n" +
                "}";

        String expected = ".THIS {" +
                "-moz-box-sizing:border-box; " +
                "box-sizing:border-box; " +
                "background:-webkit-linear-gradient(red, yellow); " +
                "background:linear-gradient(red, yellow)" +
                "}";

        assertStyle(addStyleDef(src), expected);
    }

    /** test that cross referencing self var works */
    public void testSelfCrossReference() throws Exception {
        addNsTheme(theme().var("color", "red").var("bg", "{!color}"));
        String src = ".THIS {background: theme(bg);}";
        assertStyle(addStyleDef(src), ".THIS {background:red}");
    }

    /** test that multiple level cross references work */
    public void testMultiCrossReference() throws Exception {
        addNsTheme(theme()
                .var("bright", "purple")
                .var("color", "{!bright}")
                .var("bg", "{!color}"));
        String src = ".THIS {background: theme(bg);}";
        assertStyle(addStyleDef(src), ".THIS {background:purple}");
    }

    /** test using local themes */
    public void testLocalThemes() throws Exception {
        addNsTheme(theme().var("brandColor", "red"));
        DefDescriptor<StyleDef> styleDef = addStyleDef(".THIS {color:theme(myColor)}");
        addLocalTheme(theme().var("myColor", "{!brandColor}"), styleDef);
        assertStyle(styleDef, ".THIS {color:red}");
    }

    /** test that the correct override theme is used under various combinations of which files exist */
    public void testAppOverrideThemeWhenExplicitOnly() throws Exception {
        // component in a different ns, with a ns default theme
        addNsThemeOtherNamespace(theme().var("color", "red"));
        DefDescriptor<StyleDef> toTest = addStyleDefOtherNamespace(".THIS {color: theme(color)}");

        // our app is in a different ns from the cmp, specifying an explicit override theme
        DefDescriptor<ThemeDef> override = addSeparateTheme(theme().var("color", "blue"));
        addContextApp(String.format("<aura:application overrideTheme='%s'/>", override.getDescriptorName()));

        // the explicit theme should override the components namespace-default theme
        assertStyle(toTest, ".THIS {color:blue}");
    }

    /** test that the correct override theme is used under various combinations of which files exist */
    public void testAppOverrideThemeWhenBundleOnly() throws Exception {
        // component in a different ns, with a ns default theme
        addNsThemeOtherNamespace(theme().var("color", "red"));
        DefDescriptor<StyleDef> toTest = addStyleDefOtherNamespace(".THIS {color: theme(color)}");

        // our app is in a different ns from the cmp, using an app bundle theme
        addContextAppBundleTheme(theme().var("color", "blue"));
        addContextApp("<aura:application/>");

        // the bundle theme should override the components namespace-default theme
        assertStyle(toTest, ".THIS {color:blue}");
    }

    /** test that the correct override theme is used under various combinations of which files exist */
    public void testAppOverrideThemeWhenImplicitOnly() throws Exception {
        // component in a different ns, with a ns default theme
        addNsThemeOtherNamespace(theme().var("color", "red"));
        DefDescriptor<StyleDef> toTest = addStyleDefOtherNamespace(".THIS {color: theme(color)}");

        // our app is in a different ns from the cmp, falls back to it's namespace-default theme
        addNsTheme(theme().var("color", "blue"));
        addContextApp("<aura:application/>");

        // the app's namespace-default theme should override the components namespace-default theme
        assertStyle(toTest, ".THIS {color:blue}");
    }

    /** test that the correct override theme is used under various combinations of which files exist */
    public void testAppOverrideThemeWhenNoOverride() throws Exception {
        // component in a different ns, with a ns default theme
        addNsThemeOtherNamespace(theme().var("color", "red"));
        DefDescriptor<StyleDef> toTest = addStyleDefOtherNamespace(".THIS {color: theme(color)}");

        // our app is in a different ns from the cmp, with no override theme
        addContextApp(String.format("<aura:application overrideTheme=''/>"));

        // no override should be set, so it should fallback to the cmp's namespace-default theme
        assertStyle(toTest, ".THIS {color:red}");
    }

    /** test that the correct override theme is used under various combinations of which files exist */
    public void testAppOverrideThemeWhenExplicitAndBundle() throws Exception {
        // component in a different ns, with a ns default theme
        addNsThemeOtherNamespace(theme().var("color", "red"));
        DefDescriptor<StyleDef> toTest = addStyleDefOtherNamespace(".THIS {color: theme(color)}");

        // a bundle theme exists
        addContextAppBundleTheme(theme().var("color", "blue"));

        // our app is in a different ns from the cmp, specifying an explicit override theme
        DefDescriptor<ThemeDef> override = addSeparateTheme(theme().var("color", "green"));
        addContextApp(String.format("<aura:application overrideTheme='%s'/>", override.getDescriptorName()));

        // the explicit theme outranks the cmp's namespace-default and the app's bundle theme
        assertStyle(toTest, ".THIS {color:green}");
    }

    /** test that the correct override theme is used under various combinations of which files exist */
    public void testAppOverrideThemeWhenExplicitAndImplicit() throws Exception {
        // component in a different ns, with a ns default theme
        addNsThemeOtherNamespace(theme().var("color", "red"));
        DefDescriptor<StyleDef> toTest = addStyleDefOtherNamespace(".THIS {color: theme(color)}");

        // a namespace-default theme exists in the app's namespace
        addNsTheme(theme().var("color", "blue"));

        // our app is in a different ns from the cmp, specifying an explicit override theme
        DefDescriptor<ThemeDef> override = addSeparateTheme(theme().var("color", "green"));
        addContextApp(String.format("<aura:application overrideTheme='%s'/>", override.getDescriptorName()));

        // the explicit theme outranks the cmp's namespace-default and the app's bundle theme
        assertStyle(toTest, ".THIS {color:green}");
    }

    /** test that the correct override theme is used under various combinations of which files exist */
    public void testAppOverrideThemeWhenBundleAndImplicit() throws Exception {
        // component in a different ns, with a ns default theme
        addNsThemeOtherNamespace(theme().var("color", "red"));
        DefDescriptor<StyleDef> toTest = addStyleDefOtherNamespace(".THIS {color: theme(color)}");

        // a namespace-default theme exists in the app's namespace
        addNsTheme(theme().var("color", "blue"));

        // a bundle theme exists
        addContextAppBundleTheme(theme().var("color", "green"));

        // our app is in a different ns from the cmp
        addContextApp("<aura:application/>");

        // the bundle theme outranks the app's namespace-default theme and the cmp's namespace-default theme
        assertStyle(toTest, ".THIS {color:green}");
    }

    /** test that the correct override theme is used under various combinations of which files exist */
    public void testAppOverrideThemeWhenExplicitBundleAndImplicit() throws Exception {
        // component in a different ns, with a ns default theme
        addNsThemeOtherNamespace(theme().var("color", "red"));
        DefDescriptor<StyleDef> toTest = addStyleDefOtherNamespace(".THIS {color: theme(color)}");

        // a namespace-default theme exists in the app's namespace
        addNsTheme(theme().var("color", "blue"));

        // a bundle theme exists
        addContextAppBundleTheme(theme().var("color", "green"));

        // our app is in a different ns from the cmp, specifying an explicit override theme
        DefDescriptor<ThemeDef> override = addSeparateTheme(theme().var("color", "orange"));
        addContextApp(String.format("<aura:application overrideTheme='%s'/>", override.getDescriptorName()));

        // the explicit theme outranks the other themes
        assertStyle(toTest, ".THIS {color:orange}");
    }

    /** test when an override theme doesn't define a var for a cmp in another namespace */
    public void testOverrideThemeDoesntContainVar() throws Exception {
        // component in a different ns, with a ns default theme
        addNsThemeOtherNamespace(theme().var("color", "red"));
        DefDescriptor<StyleDef> toTest = addStyleDefOtherNamespace(".THIS {color: theme(color)}");

        // our app is in a different ns from the cmp, specifying an explicit override theme
        DefDescriptor<ThemeDef> override = addSeparateTheme(theme().var("font", "arial"));
        addContextApp(String.format("<aura:application overrideTheme='%s'/>", override.getDescriptorName()));

        // the cmp should fallback to it's namespace-default theme, without an error
        assertStyle(toTest, ".THIS {color:red}");
    }

    /** test that an override theme inherited var is applied */
    public void testExplicitOverrideThemeInheritedVar() throws Exception {
        // component in a different ns, with a ns default theme
        addNsThemeOtherNamespace(theme().var("color", "red"));
        DefDescriptor<StyleDef> toTest = addStyleDefOtherNamespace(".THIS {color: theme(color)}");

        // the explicit theme will inherit from this one
        DefDescriptor<ThemeDef> parent = addSeparateTheme(theme().var("color", "blue"));

        // our app is in a different ns from the cmp, specifying an explicit override theme
        DefDescriptor<ThemeDef> override = addSeparateTheme(theme().parent(parent).var("font", "arial"));
        addContextApp(String.format("<aura:application overrideTheme='%s'/>", override.getDescriptorName()));

        // the inherited value should be used
        assertStyle(toTest, ".THIS {color:blue}");
    }

    /** test that an override theme inherited var is applied */
    public void testImplicitOverrideThemeInheritedVar() throws Exception {
        // component in a different ns, with a ns default theme
        addNsThemeOtherNamespace(theme().var("color", "red"));
        DefDescriptor<StyleDef> toTest = addStyleDefOtherNamespace(".THIS {color: theme(color)}");

        // the namespace-default theme for the app inherits the var
        DefDescriptor<ThemeDef> parent = addSeparateTheme(theme().var("color", "blue"));
        addNsTheme(theme().parent(parent));

        // our app is in a different ns from the cmp
        addContextApp("<aura:application/>");

        // the inherited value on the implicit theme should be used
        assertStyle(toTest, ".THIS {color:blue}");
    }

    /** test when an implicit theme inherits from the other cmp's default theme */
    public void testImplicitOverrideThemeCircleAround() throws Exception {
        // component in a different ns, with a ns default theme
        DefDescriptor<ThemeDef> otherTheme = addNsThemeOtherNamespace(theme().var("color", "red"));
        DefDescriptor<StyleDef> toTest = addStyleDefOtherNamespace(".THIS {color: theme(color)}");

        // the app's implicit theme inherits from the cmp's namespace-default theme
        addNsTheme(theme().parent(otherTheme));

        // our app is in a different ns from the cmp
        addContextApp("<aura:application/>");

        // the effect should be the same as if the override wasn't there
        assertStyle(toTest, ".THIS {color:red}");
    }

    /** test when an implicit theme inherits from the other cmp's default theme, but then changes the val */
    public void testImplicitOverrideThemeCircleAroundChanged() throws Exception {
        // component in a different ns, with a ns default theme
        DefDescriptor<ThemeDef> otherTheme = addNsThemeOtherNamespace(theme().var("color", "red"));
        DefDescriptor<StyleDef> toTest = addStyleDefOtherNamespace(".THIS {color: theme(color)}");

        // the app's implicit theme inherits from the cmp's namespace-default theme
        addNsTheme(theme().parent(otherTheme).var("color", "blue"));

        // our app is in a different ns from the cmp
        addContextApp("<aura:application/>");

        // the app's implicit theme's value should be used
        assertStyle(toTest, ".THIS {color:blue}");
    }
}
