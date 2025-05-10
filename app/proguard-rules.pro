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