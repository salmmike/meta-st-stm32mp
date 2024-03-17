SUMMARY = "Linux STM32MP Kernel"
SECTION = "kernel"
LICENSE = "GPL-2.0-only"
#LIC_FILES_CHKSUM = "file://COPYING;md5=bbea815ee2795b2f4230826c0c6b8814"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

include linux-stm32mp.inc

LINUX_VERSION = "6.1"
LINUX_SUBVERSION = ".28"
LINUX_TARBASE = "linux-${LINUX_VERSION}${LINUX_SUBVERSION}"
LINUX_TARNAME = "${LINUX_TARBASE}.tar.xz"

SRC_URI = "git://github.com/salmmike/linux-stable.git;branch=dev/sandbox;protocol=https"
BB_STRICT_CHECKSUM = "0"
SRCREV = "${AUTOREV}"

LINUX_TARGET = "stm32mp"
LINUX_RELEASE = "r1.1"

PV = "${LINUX_VERSION}${LINUX_SUBVERSION}-${LINUX_TARGET}-${LINUX_RELEASE}"

ARCHIVER_ST_BRANCH = "v${LINUX_VERSION}-${LINUX_TARGET}"
ARCHIVER_ST_REVISION = "v${LINUX_VERSION}-${LINUX_TARGET}-${LINUX_RELEASE}"
ARCHIVER_COMMUNITY_BRANCH = "linux-${LINUX_VERSION}.y"
ARCHIVER_COMMUNITY_REVISION = "v${LINUX_VERSION}${LINUX_SUBVERSION}"

S = "${WORKDIR}/git"

# ---------------------------------
# Configure devupstream class usage
# ---------------------------------
BBCLASSEXTEND = "devupstream:target"

SRC_URI:class-devupstream = "git://github.com/STMicroelectronics/linux.git;protocol=https;branch=${ARCHIVER_ST_BRANCH}"
SRCREV:class-devupstream = "47937a24f0ed893d100fe94a566c6bd83dd78de5"
#FIXME force the PV to avoid build issue:
#  do_package: ExpansionError('SRCPV', '${@bb.fetch2.get_srcrev(d)}', FetchError('SRCREV was used yet no valid SCM was found in SRC_URI', None))
PV:class-devupstream = "${LINUX_VERSION}${LINUX_SUBVERSION}-${LINUX_TARGET}.${SRCPV}"

# ---------------------------------
# Configure default preference to manage dynamic selection between tarball and github
# ---------------------------------
STM32MP_SOURCE_SELECTION ?= "tarball"

DEFAULT_PREFERENCE = "${@bb.utils.contains('STM32MP_SOURCE_SELECTION', 'github', '-1', '1', d)}"

# ---------------------------------
# Configure archiver use
# ---------------------------------
include ${@oe.utils.ifelse(d.getVar('ST_ARCHIVER_ENABLE') == '1', 'linux-stm32mp-archiver.inc','')}

# -------------------------------------------------------------
# Defconfig
#
KERNEL_DEFCONFIG        = "defconfig"
KERNEL_CONFIG_FRAGMENTS = "${@bb.utils.contains('KERNEL_DEFCONFIG', 'defconfig', '${S}/arch/arm/configs/fragment-01-multiv7_cleanup.config', '', d)}"
KERNEL_CONFIG_FRAGMENTS += "${@bb.utils.contains('KERNEL_DEFCONFIG', 'defconfig', '${S}/arch/arm/configs/fragment-02-multiv7_addons.config', '', d)}"
KERNEL_CONFIG_FRAGMENTS += "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '${WORKDIR}/fragments/${LINUX_VERSION}/fragment-03-systemd.config', '', d)} "
KERNEL_CONFIG_FRAGMENTS += "${WORKDIR}/fragments/${LINUX_VERSION}/fragment-04-modules.config"
KERNEL_CONFIG_FRAGMENTS += "${@oe.utils.ifelse(d.getVar('KERNEL_SIGN_ENABLE') == '1', '${WORKDIR}/fragments/features/${LINUX_VERSION}/optional-fragment-05-signature.config','')} "
KERNEL_CONFIG_FRAGMENTS += "${@bb.utils.contains('MACHINE_FEATURES', 'nosmp', '${WORKDIR}/fragments/features/${LINUX_VERSION}/optional-fragment-06-nosmp.config', '', d)} "
KERNEL_CONFIG_FRAGMENTS += "${@bb.utils.contains('MACHINE_FEATURES', 'efi', '${WORKDIR}/fragments/features/${LINUX_VERSION}/optional-fragment-07-efi.config', '', d)} "

SRC_URI += "file://${LINUX_VERSION}/fragment-03-systemd.config;subdir=fragments"
SRC_URI += "file://${LINUX_VERSION}/fragment-04-modules.config;subdir=fragments"
SRC_URI += "file://${LINUX_VERSION}/optional-fragment-05-signature.config;subdir=fragments/features"
SRC_URI += "file://${LINUX_VERSION}/optional-fragment-06-nosmp.config;subdir=fragments/features"
SRC_URI += "file://${LINUX_VERSION}/optional-fragment-07-efi.config;subdir=fragments/features"

# Don't forget to add/del for devupstream
SRC_URI:class-devupstream += "file://${LINUX_VERSION}/fragment-03-systemd.config;subdir=fragments"
SRC_URI:class-devupstream += "file://${LINUX_VERSION}/fragment-04-modules.config;subdir=fragments"
SRC_URI:class-devupstream += "file://${LINUX_VERSION}/optional-fragment-05-signature.config;subdir=fragments/features"
SRC_URI:class-devupstream += "file://${LINUX_VERSION}/optional-fragment-06-nosmp.config;subdir=fragments/features"
SRC_URI:class-devupstream += "file://${LINUX_VERSION}/optional-fragment-07-efi.config;subdir=fragments/features"

# -------------------------------------------------------------
# Kernel Args
#
KERNEL_EXTRA_ARGS += "LOADADDR=${ST_KERNEL_LOADADDR}"
