package catgirlroutes.utils.customtriggers

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class TypeName(val value: String)