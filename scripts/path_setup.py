# Find the right paths:
import pathlib

ROOT = pathlib.Path(__file__).resolve().parents[1]

CUPTEST = ROOT / "CupTest"
BUILD_CLASSES = CUPTEST / "app" / "build" / "classes" / "java" / "main"
BUILD_RES = CUPTEST / "app" / "build" / "resources" / "main"
JPF_JAR = ROOT / "jpf-core" / "build" / "jpf.jar"
JPF_JAR_FOLDER = ROOT / "jpf-core" / "build"

JACONTEBE_ROOT = ROOT / "JaConTeBe"

SCTBENCH = ROOT / "SctBench"

SOOT = ROOT / "soot"


CONFIGS_DIR = ROOT / "configs"
