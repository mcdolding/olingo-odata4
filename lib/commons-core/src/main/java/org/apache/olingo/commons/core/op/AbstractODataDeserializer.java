/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.commons.core.op;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.domain.ODataError;
import org.apache.olingo.commons.api.data.EntitySet;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.format.ODataFormat;
import org.apache.olingo.commons.api.format.ODataPubFormat;
import org.apache.olingo.commons.api.edm.constants.ODataServiceVersion;
import org.apache.olingo.commons.api.op.CommonODataDeserializer;
import org.apache.olingo.commons.core.data.AtomDeserializer;
import org.apache.olingo.commons.api.data.ResWrap;
import org.apache.olingo.commons.core.data.AtomEntityImpl;
import org.apache.olingo.commons.core.data.AtomEntitySetImpl;
import org.apache.olingo.commons.core.data.AtomPropertyImpl;
import org.apache.olingo.commons.core.data.JSONEntityImpl;
import org.apache.olingo.commons.core.data.JSONEntitySetImpl;
import org.apache.olingo.commons.core.data.JSONODataErrorImpl;
import org.apache.olingo.commons.core.data.JSONPropertyImpl;
import org.apache.olingo.commons.core.data.XMLODataErrorImpl;

public abstract class AbstractODataDeserializer extends AbstractJacksonTool implements CommonODataDeserializer {

  private static final long serialVersionUID = -4244158979195609909L;

  private final AtomDeserializer atomDeserializer;

  public AbstractODataDeserializer(final ODataServiceVersion version) {
    super(version);

    this.atomDeserializer = new AtomDeserializer(version);
  }

  @Override
  public ResWrap<EntitySet> toEntitySet(final InputStream input, final ODataPubFormat format) {
    return format == ODataPubFormat.ATOM
            ? this.<EntitySet, AtomEntitySetImpl>atom(input, AtomEntitySetImpl.class)
            : this.<EntitySet, JSONEntitySetImpl>json(input, JSONEntitySetImpl.class);
  }

  @Override
  public ResWrap<Entity> toEntity(final InputStream input, final ODataPubFormat format) {
    return format == ODataPubFormat.ATOM
            ? this.<Entity, AtomEntityImpl>atom(input, AtomEntityImpl.class)
            : this.<Entity, JSONEntityImpl>json(input, JSONEntityImpl.class);
  }

  @Override
  public ResWrap<Property> toProperty(final InputStream input, final ODataFormat format) {
    return format == ODataFormat.XML
            ? this.<Property, AtomPropertyImpl>atom(input, AtomPropertyImpl.class)
            : this.<Property, JSONPropertyImpl>json(input, JSONPropertyImpl.class);
  }

  @Override
  public ODataError toError(final InputStream input, final boolean isXML) {
    return isXML
            ? this.<ODataError, XMLODataErrorImpl>atom(input, XMLODataErrorImpl.class).getPayload()
            : this.<ODataError, JSONODataErrorImpl>json(input, JSONODataErrorImpl.class).getPayload();
  }

  /*
   * ------------------ Protected methods ------------------
   */
  protected <T, V extends T> ResWrap<T> atom(final InputStream input, final Class<V> reference) {
    try {
      return atomDeserializer.<T, V>read(input, reference);
    } catch (Exception e) {
      throw new IllegalArgumentException("While deserializing " + reference.getName(), e);
    }
  }

  @SuppressWarnings("unchecked")
  protected <T, V extends T> ResWrap<T> xml(final InputStream input, final Class<V> reference) {
    try {
      final T obj = getXmlMapper().readValue(input, new TypeReference<V>() {
        @Override
        public Type getType() {
          return reference;
        }
      });

      return obj instanceof ResWrap ? (ResWrap<T>) obj : new ResWrap<T>((URI) null, null, obj);
    } catch (Exception e) {
      throw new IllegalArgumentException("While deserializing " + reference.getName(), e);
    }
  }

  @SuppressWarnings("unchecked")
  protected <T, V extends T> ResWrap<T> json(final InputStream input, final Class<V> reference) {
    try {
      final T obj = getObjectMapper().readValue(input, new TypeReference<V>() {
        @Override
        public Type getType() {
          return reference;
        }
      });

      return obj instanceof ResWrap ? (ResWrap<T>) obj : new ResWrap<T>((URI) null, null, obj);
    } catch (Exception e) {
      throw new IllegalArgumentException("While deserializing " + reference.getName(), e);
    }
  }
}
