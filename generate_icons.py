#!/usr/bin/env python3
"""
Script para convertir una imagen 1000x1000 a iconos Android en mipmap.
Requiere: pip install Pillow

Uso: python generate_icons.py input.png
"""

import sys
import os
from PIL import Image

# Tamaños Android (densidad de píxeles)
SIZES = {
    "mdpi": 48,    # 1x
    "hdpi": 72,    # 1.5x
    "xhdpi": 96,   # 2x
    "xxhdpi": 144, # 3x
    "xxxhdpi": 192 # 4x
}

def generate_icons(input_path, output_dir="app/src/main/res"):
    """Genera iconos para todas las densidades Android."""
    
    if not os.path.exists(input_path):
        print(f"Error: {input_path} no existe")
        return
    
    # Abrir imagen
    img = Image.open(input_path)
    
    # Redimensionar a 1000x1000 si es necesario
    if img.size != (1000, 1000):
        print("Redimensionando imagen a 1000x1000")
        img = img.resize((1000, 1000), Image.LANCZOS)
    
    # Convertir a RGB si tiene transparencia
    if img.mode in ("RGBA", "LA"):
        background = Image.new("RGB", img.size, (255, 255, 255))
        background.paste(img, mask=img.split()[-1])
        img = background
    
    # Generar iconos
    for density, size in SIZES.items():
        # Calcular escala
        scale = size / 48  # mdpi es 48px
        new_size = int(1000 * scale / 4)  # 1000px es ~4x mdpi
        
        # Redimensionar
        icon = img.resize((new_size, new_size), Image.LANCZOS)
        
        # Crear directorio
        mipmap_dir = os.path.join(output_dir, f"mipmap-{density}")
        os.makedirs(mipmap_dir, exist_ok=True)
        
        # Guardar
        output_path = os.path.join(mipmap_dir, "ic_launcher.png")
        icon.save(output_path, "PNG")
        print(f"Generado: {output_path} ({new_size}x{new_size})")
    
    # También crear para mipmap (por defecto es xxhdpi)
    mipmap_dir = os.path.join(output_dir, "mipmap")
    os.makedirs(mipmap_dir, exist_ok=True)
    icon = img.resize((SIZES["xxhdpi"], SIZES["xxhdpi"]), Image.LANCZOS)
    icon.save(os.path.join(mipmap_dir, "ic_launcher.png"), "PNG")
    print(f"Generado: {mipmap_dir}/ic_launcher.png")
    
    print("\n¡Iconos generados exitosamente!")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Uso: python generate_icons.py input.png [output_dir]")
        sys.exit(1)
    
    input_file = sys.argv[1]
    output_directory = sys.argv[2] if len(sys.argv) > 2 else "app/src/main/res"
    generate_icons(input_file, output_directory)