package dev.schuberth.template.lib

import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe

class LibTest : WordSpec({
    "sayHello()" should {
        "say hello" {
            sayHello() shouldBe "Hello world!"
        }
    }
})
