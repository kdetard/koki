package io.github.kdetard.koki.di;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import dev.onenowy.moshipolymorphicadapter.ValuePolymorphicAdapterFactory;
import io.github.kdetard.koki.keycloak.models.KeycloakToken;
import io.github.kdetard.koki.network.models.CookieJsonAdapter;
import io.github.kdetard.koki.openremote.models.Asset;
import io.github.kdetard.koki.openremote.models.Attribute;
import io.github.kdetard.koki.openremote.models.OpenRemoteModel;
import io.github.kdetard.koki.openremote.models.OpenRemoteObject;
import io.github.kdetard.koki.openremote.models.Widget;

@Module
@InstallIn(SingletonComponent.class)
public class SerdeModule {
    @Provides
    @Singleton
    public static Moshi provideMoshi() {
        return new Moshi.Builder()
                .add(new CookieJsonAdapter())
                .add(ValuePolymorphicAdapterFactory.of(OpenRemoteObject.class, "type", String.class)
                        .withSubtypes(OpenRemoteModel.ObjectClasses, OpenRemoteModel.ObjectSubTypes))
                .add(ValuePolymorphicAdapterFactory.of(Asset.class, "type", String.class)
                        .withSubtypes(OpenRemoteModel.AssetClasses, OpenRemoteModel.AssetSubTypes))
                .add(ValuePolymorphicAdapterFactory.of(Attribute.class, "type", String.class)
                        .withSubtypes(OpenRemoteModel.AttributeClasses, OpenRemoteModel.AttributeSubTypes))
                .add(ValuePolymorphicAdapterFactory.of(Widget.class, "widgetTypeId", String.class)
                        .withSubtypes(OpenRemoteModel.WidgetClasses, OpenRemoteModel.WidgetSubTypes))
                .build();
    }

    @Provides
    @Singleton
    public static JsonAdapter<KeycloakToken> provideKeycloakTokenJsonAdapter(final Moshi moshi) {
        return moshi.adapter(KeycloakToken.class).nullSafe();
    }

    @Provides
    @Singleton
    public static JsonAdapter<Object> provideObjectJsonAdapter(final Moshi moshi) {
        return moshi.adapter(Object.class).nullSafe();
    }
}
