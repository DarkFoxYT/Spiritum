# Temporary code-authored pixel textures for the poppet and argent sentinel.
Add-Type -AssemblyName System.Drawing
$assetRoot = Join-Path (Split-Path -Parent $PSScriptRoot) 'src/main/resources/assets/spiritum/textures'
New-Item -ItemType Directory -Force -Path "$assetRoot/entity", "$assetRoot/item" | Out-Null
foreach ($kind in @('poppet', 'sentinel')) {
    $height = if ($kind -eq 'poppet') { 32 } else { 64 }
    $bitmap = [System.Drawing.Bitmap]::new(64, $height)
    for ($y = 0; $y -lt $height; $y++) {
        for ($x = 0; $x -lt 64; $x++) {
            $seam = ($x % 8 -eq 0) -or ($y % 8 -eq 0)
            if ($kind -eq 'poppet') {
                $color = if ($seam) { [System.Drawing.Color]::FromArgb(112, 58, 62) } else { [System.Drawing.Color]::FromArgb(181 + (($x + $y) % 3) * 6, 124, 123) }
            } else {
                $color = if ($seam) { [System.Drawing.Color]::FromArgb(68, 77, 97) } else { [System.Drawing.Color]::FromArgb(177 + (($x + $y) % 4) * 8, 188, 211) }
                if ($y -ge 5 -and $y -le 6 -and $x -ge 8 -and $x -le 15) { $color = [System.Drawing.Color]::FromArgb(112, 233, 239) }
            }
            $bitmap.SetPixel($x, $y, $color)
        }
    }
    $bitmap.Save("$assetRoot/entity/$kind.png", [System.Drawing.Imaging.ImageFormat]::Png)
    $bitmap.Dispose()
}
$icon = [System.Drawing.Bitmap]::new(16, 16)
for ($y = 1; $y -lt 15; $y++) {
    for ($x = 3; $x -lt 13; $x++) {
        $visible = if ($y -lt 5) { $x -ge 6 -and $x -le 9 } elseif ($y -lt 10) { $true } else { ($x -ge 5 -and $x -le 7) -or ($x -ge 9 -and $x -le 11) }
        if ($visible) {
            $color = if ($y -eq 3) { [System.Drawing.Color]::FromArgb(112, 233, 239) } elseif ($x % 3 -eq 0) { [System.Drawing.Color]::FromArgb(68, 77, 97) } else { [System.Drawing.Color]::FromArgb(193, 203, 223) }
            $icon.SetPixel($x, $y, $color)
        }
    }
}
$icon.Save("$assetRoot/item/sentinel.png", [System.Drawing.Imaging.ImageFormat]::Png)
$icon.Dispose()

New-Item -ItemType Directory -Force -Path "$assetRoot/particle" | Out-Null
foreach ($rune in @('dominion_rune','dominion_rite','vigilance_rite')) {
    $bitmap = [System.Drawing.Bitmap]::new(32, 32)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $pen = [System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb(180, 142, 255), 2)
    $graphics.DrawPolygon($pen, [System.Drawing.Point[]]@([System.Drawing.Point]::new(16,2),[System.Drawing.Point]::new(29,16),[System.Drawing.Point]::new(16,29),[System.Drawing.Point]::new(2,16)))
    $graphics.DrawLine($pen, 16, 6, 16, 25)
    if ($rune -eq 'vigilance_rite') { $graphics.DrawEllipse($pen, 7, 10, 18, 10) }
    else { $graphics.DrawLine($pen, 8, 11, 24, 11); $graphics.DrawLine($pen, 8, 21, 24, 21) }
    $bitmap.Save("$assetRoot/particle/$rune.png", [System.Drawing.Imaging.ImageFormat]::Png)
    $graphics.Dispose(); $pen.Dispose(); $bitmap.Dispose()
}
