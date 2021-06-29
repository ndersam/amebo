/*
 * Copyright (c) 2014, Inversoft Inc., All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.amebo.core.converter.service;

import android.content.Context;

import androidx.annotation.RestrictTo;
import androidx.core.util.Predicate;

import com.amebo.core.common.extensions.ContextExtKt;
import com.amebo.core.converter.domain.Document;
import com.amebo.core.converter.domain.TagNode;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * BBCode to HTML Transformer.
 * <p>
 * This implementation utilizes the provided Mustache templates and does not require the caller to provide their own.
 * <p>
 * This a also means the caller has less control over the output. For more control over the generated HTML, provide your
 * own Mustache templates and use the {@link MustacheTransformer} class or by using the provided factory.
 *
 * @author Nder Sesugh
 * Based on FreeMarker implementation from https://github.com/inversoft/prime-transformer
 */
public class BBCodeToHTMLTransformer implements Transformer {
    private static final String TEMPLATE_DIR = "toHTML";
    private static final Map<String, Template> DEFAULT_TEMPLATES = new HashMap<>();

    public static void initialize(Context context) {
        if (!DEFAULT_TEMPLATES.isEmpty()) {
            return;
        }
        ContextLoader loader = new ContextLoader(context);
        fetchTemplates(loader);
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public static void initialize() {
        if (!DEFAULT_TEMPLATES.isEmpty()) {
            return;
        }
        Loader loader = new Loader();
        fetchTemplates(loader);
    }

    private static void fetchTemplates(ILoader loader) {
        DEFAULT_TEMPLATES.put("b", loader.fetchTemplate("bold"));
        DEFAULT_TEMPLATES.put("i", loader.fetchTemplate("italic"));
        DEFAULT_TEMPLATES.put("u", loader.fetchTemplate("underline"));
        DEFAULT_TEMPLATES.put("s", loader.fetchTemplate("strikethrough"));
        DEFAULT_TEMPLATES.put("*", loader.fetchTemplate("item"));
        DEFAULT_TEMPLATES.put("li", loader.fetchTemplate("item"));
        DEFAULT_TEMPLATES.put("list", loader.fetchTemplate("list"));
        DEFAULT_TEMPLATES.put("ul", loader.fetchTemplate("list"));
        DEFAULT_TEMPLATES.put("ol", loader.fetchTemplate("ol"));
        DEFAULT_TEMPLATES.put("url", loader.fetchTemplate("url"));
        DEFAULT_TEMPLATES.put("table", loader.fetchTemplate("table"));
        DEFAULT_TEMPLATES.put("tr", loader.fetchTemplate("tr"));
        DEFAULT_TEMPLATES.put("td", loader.fetchTemplate("td"));
        DEFAULT_TEMPLATES.put("code", loader.fetchTemplate("code"));
        DEFAULT_TEMPLATES.put("quote", loader.fetchTemplate("quote"));
        DEFAULT_TEMPLATES.put("email", loader.fetchTemplate("email"));
        DEFAULT_TEMPLATES.put("img", loader.fetchTemplate("image"));
        DEFAULT_TEMPLATES.put("size", loader.fetchTemplate("size"));
        DEFAULT_TEMPLATES.put("sub", loader.fetchTemplate("sub"));
        DEFAULT_TEMPLATES.put("sup", loader.fetchTemplate("sup"));
        DEFAULT_TEMPLATES.put("noparse", loader.fetchTemplate("noparse"));
        DEFAULT_TEMPLATES.put("color", loader.fetchTemplate("color"));
        DEFAULT_TEMPLATES.put("left", loader.fetchTemplate("left"));
        DEFAULT_TEMPLATES.put("center", loader.fetchTemplate("center"));
        DEFAULT_TEMPLATES.put("right", loader.fetchTemplate("right"));
        DEFAULT_TEMPLATES.put("th", loader.fetchTemplate("th"));
        DEFAULT_TEMPLATES.put("font", loader.fetchTemplate("font"));
        DEFAULT_TEMPLATES.put("hr", loader.fetchTemplate("hr"));
    }


    private final MustacheTransformer transformer;

    public BBCodeToHTMLTransformer() {
        this(false);
    }

    public BBCodeToHTMLTransformer(boolean strict) {
        this.transformer = new MustacheTransformer(DEFAULT_TEMPLATES, strict);
    }

    @Override
    public String transform(Document document, Predicate<TagNode> transformPredicate, TransformFunction transformFunction,
                            NodeConsumer nodeConsumer) throws TransformException {
        return transformer.transform(document, transformPredicate, transformFunction, nodeConsumer);
    }

    private static class Loader implements ILoader {
        private final Mustache.Compiler c;

        Loader() {
            c = Mustache.compiler();
        }

        private String readTemplate(String filename) {
            try {
                return openRes(filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        private String openRes(String filename) throws IOException {
            InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(TEMPLATE_DIR + "/" + filename);
            byte[] bytes = new byte[is.available()];
            is.read(bytes, 0, is.available());
            return new String(bytes);
        }

        @Override
        public Template fetchTemplate(String name) {
            return c.compile(readTemplate(name + ".mustache"));
        }
    }

    private static class ContextLoader implements ILoader {
        private final Mustache.Compiler c;
        private final Context context;

        private ContextLoader(Context context) {
            c = Mustache.compiler().escapeHTML(false);
            this.context = context;
        }

        private String readTemplate(String filename) {
            try {
                return ContextExtKt.openRawAsString(context, filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public Template fetchTemplate(String name) {
            return c.compile(readTemplate(name));
        }
    }

    public interface ILoader {
        Template fetchTemplate(String name);
    }
}
