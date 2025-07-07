# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-dontwarn edu.umd.cs.findbugs.annotations.SuppressFBWarnings
-dontwarn java.awt.font.FontRenderContext
-dontwarn java.awt.font.LineBreakMeasurer
-dontwarn java.awt.font.TextLayout
-dontwarn java.awt.geom.AffineTransform
-dontwarn java.beans.BeanDescriptor
-dontwarn java.beans.BeanInfo
-dontwarn java.beans.IntrospectionException
-dontwarn java.beans.Introspector
-dontwarn java.beans.PropertyDescriptor
-dontwarn javax.imageio.ImageIO
-dontwarn javax.imageio.ImageReader
-dontwarn javax.imageio.stream.ImageInputStream
-dontwarn javax.lang.model.SourceVersion
-dontwarn javax.naming.InvalidNameException
-dontwarn javax.naming.NamingException
-dontwarn javax.naming.directory.Attribute
-dontwarn javax.naming.directory.Attributes
-dontwarn javax.naming.ldap.LdapName
-dontwarn javax.naming.ldap.Rdn
-dontwarn javax.servlet.ServletContainerInitializer
-dontwarn org.apache.bsf.BSFManager
-dontwarn org.ietf.jgss.GSSContext
-dontwarn org.ietf.jgss.GSSCredential
-dontwarn org.ietf.jgss.GSSException
-dontwarn org.ietf.jgss.GSSManager
-dontwarn org.ietf.jgss.GSSName
-dontwarn org.ietf.jgss.Oid

# Ktor
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.atomicfu.**
-dontwarn io.netty.**
-dontwarn com.typesafe.**
-dontwarn org.slf4j.*


# Mantieni le classi serializzabili di Kotlinx
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Regole speciali per kotlinx.serialization
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Mantieni le classi `kotlinx.serialization` per il runtime
-keep class kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.** {
    <methods>;
}

# Regole specifiche per Ktor
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.atomicfu.**
-dontwarn io.netty.**
-dontwarn com.typesafe.**
-dontwarn org.slf4j.**

# Se usi il motore CIO
-keep class io.ktor.client.engine.cio.** { *; }

# Se usi il motore OkHttp
-keep class io.ktor.client.engine.okhttp.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Se usi il motore Android
-keep class io.ktor.client.engine.android.** { *; }

# Per i modelli di dati che usi con Ktor
# Sostituisci com.tuodominio.app.models con il tuo package
-keep class com.tuodominio.app.models.** { *; }

# Mantieni le annotazioni di serializzazione
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Per le configurazioni di logging in Ktor
-dontwarn org.slf4j.**
-dontwarn ch.qos.logback.**

# Per il supporto a JSON
-keep class kotlinx.serialization.json.** { *; }

# Per WebSockets (se li usi)
-keep class io.ktor.websocket.** { *; }

# Se usi Auth
-keep class io.ktor.client.features.auth.** { *; }

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean

# Regole generali per prevenire crash all'avvio

# Mantieni le annotazioni
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes Exceptions

# Mantieni i modelli usati nell'applicazione (adatta ai tuoi package)
-keep class it.softsamurai.models.** { *; }
-keep class it.softsamurai.data.** { *; }

# Hilt/Dagger - molto comune nei crash all'avvio
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.internal.Binding
-keep class * extends dagger.internal.ModuleAdapter
-keep class * extends dagger.internal.StaticInjection
-keep class **$$ModuleAdapter
-keep class **$$InjectAdapter
-keep class **$$StaticInjection
-keep class javax.annotation.** { *; }

# Mantieni i componenti Hilt che possono causare crash all'avvio
-keep class * extends androidx.hilt.work.HiltWorker
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel

# Regole per Compose
-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.viewmodel.compose.** { *; }

# Arrow
-keep class arrow.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Skrape.it
-keep class it.skrape.** { *; }

# Regole per Activity/Fragment/ViewModel
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}
-keepclassmembers class * extends androidx.fragment.app.Fragment {
    public void *(android.view.View);
}
-keep class androidx.lifecycle.** { *; }

# Regole per i crash legati al runtime Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Regola specifica per evitare crash con le reflection in Kotlin
-keepclasseswithmembers class * {
    @kotlin.jvm.* <methods>;
}

# Per i crash legati a Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

-keep class org.apache.commons.logging.impl.*

-dontwarn javax.servlet.ServletContextListener
-dontwarn org.apache.avalon.framework.logger.Logger
-dontwarn org.apache.log.Hierarchy
-dontwarn org.apache.log.Logger

# Regole ProGuard specifiche per SkrapeIt e le sue dipendenze

# Regole per SkrapeIt
-keep class it.skrape.** { *; }
-dontwarn it.skrape.**

# Regole per HtmlUnit (utilizzato da BrowserFetcher di SkrapeIt)
-keep class com.gargoylesoftware.htmlunit.** { *; }
-dontwarn com.gargoylesoftware.htmlunit.**

# Regole per Apache Commons Logging
-keep class org.apache.commons.logging.** { *; }
-dontwarn org.apache.commons.logging.**

# Implementazioni di Log che potrebbero essere utilizzate
-keep class org.apache.commons.logging.impl.** { *; }
-keep class org.apache.logging.log4j.** { *; }
-keep class org.slf4j.** { *; }
-dontwarn org.slf4j.**
-dontwarn org.apache.logging.log4j.**

# Altre dipendenze di HtmlUnit
-keep class org.apache.http.** { *; }
-dontwarn org.apache.http.**
-keep class org.eclipse.jetty.** { *; }
-dontwarn org.eclipse.jetty.**
-keep class org.w3c.css.** { *; }
-dontwarn org.w3c.css.**

# Per XML e simili utilizzati da HtmlUnit
-keep class org.w3c.dom.** { *; }
-keep class org.xml.** { *; }
-dontwarn org.xml.**

# Regole per JavaScript engine usato da HtmlUnit
-keep class net.sourceforge.htmlunit.corejs.** { *; }
-dontwarn net.sourceforge.htmlunit.corejs.**

# Regole per WebSocket e altre componenti di rete
-keep class org.apache.coyote.** { *; }
-dontwarn org.apache.coyote.**
-keep class org.apache.tomcat.** { *; }
-dontwarn org.apache.tomcat.**